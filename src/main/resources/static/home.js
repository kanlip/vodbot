// Encapsulate all camera logic within a global object for cleaner HTMX calls
window.cameraApp = (() => {
    // ... (existing DOM Elements and Global Variables) ...
    const cameraSelector = document.getElementById('cameraSelector');
    const startButton = document.getElementById('startButton');
    const stopButton = document.getElementById('stopButton');
    const cameraFeed = document.getElementById('cameraFeed');
    const statusDiv = document.getElementById('status');
    const mirrorCheckbox = document.getElementById('mirrorCheckbox');

    const recordButton = document.getElementById('recordButton');
    const stopRecordButton = document.getElementById('stopRecordButton');
    const downloadButton = document.getElementById('downloadButton');

    let currentStream = null;
    let selectedDeviceId = null;

    let mediaRecorder = null;
    let recordedChunks = [];
    let isRecording = false;
    let currentRecordingMimeType = 'video/webm'; // Track the actual MIME type used for recording

    // ... (updateButtonStates, populateCameraSelector, startCamera, stopCamera, toggleMirror - unchanged) ...

    // --- Helper to update button states ---
    function updateButtonStates() {
        // ... (existing logic) ...
        const hasCamera = cameraSelector.options.length > 0 && cameraSelector.value !== '';
        const canStartCamera = hasCamera && !currentStream;
        const canStopCamera = currentStream !== null;
        const canRecord = currentStream !== null && !isRecording;
        const canStopRecord = isRecording;
        // Can download if chunks exist and not actively recording
        const canDownload = recordedChunks.length > 0 && !isRecording;

        startButton.disabled = !canStartCamera;
        stopButton.disabled = !canStopCamera;
        cameraSelector.disabled = currentStream !== null; // Disable selector when camera is active

        recordButton.disabled = !canRecord;
        stopRecordButton.disabled = !canStopRecord;
        downloadButton.disabled = !canDownload;

        const currentText = statusDiv.textContent.replace(/Recording \S+ /, '').trim();
        if (isRecording) {
            if (!statusDiv.querySelector('.recording-indicator')) {
                statusDiv.innerHTML = `Recording <span class="recording-indicator"></span> ${currentText}`;
            }
        } else {
            const indicatorSpan = statusDiv.querySelector('.recording-indicator');
            if (indicatorSpan) indicatorSpan.remove();
        }
    }

    /**
     * Populates the camera selector dropdown with available video input devices.
     */
    async function populateCameraSelector() {
        cameraSelector.innerHTML = '<option value="">Loading cameras...</option>';
        updateButtonStates();

        try {
            await navigator.mediaDevices.getUserMedia({ video: true, audio: false });

            const devices = await navigator.mediaDevices.enumerateDevices();
            const videoInputDevices = devices.filter(device => device.kind === 'videoinput');

            cameraSelector.innerHTML = '';

            if (videoInputDevices.length === 0) {
                const option = document.createElement('option');
                option.value = '';
                option.textContent = 'No camera devices found.';
                cameraSelector.appendChild(option);
                statusDiv.textContent = 'Error: No cameras detected.';
                updateButtonStates();
                return;
            }

            videoInputDevices.forEach(device => {
                const option = document.createElement('option');
                option.value = device.deviceId;
                option.textContent = device.label || `Camera ${cameraSelector.options.length + 1}`;
                cameraSelector.appendChild(option);
            });

            selectedDeviceId = videoInputDevices[0].deviceId;
            cameraSelector.value = selectedDeviceId;

            statusDiv.textContent = 'Cameras loaded. Select a camera and click "Start".';
            updateButtonStates();

        } catch (err) {
            console.error('Error enumerating devices or getting initial permission:', err);
            cameraSelector.innerHTML = '<option value="">Permission Denied or Error</option>';
            statusDiv.textContent = `Error: Cannot access cameras. Please grant permission: ${err.name}`;
            updateButtonStates();
        }
    }

    /**
     * Starts the video stream from the selected camera.
     * @param {string} deviceId The ID of the camera to start.
     */
    async function startCamera(deviceId) {
        if (currentStream) {
            stopCamera();
        }

        statusDiv.textContent = 'Attempting to start camera...';
        updateButtonStates();

        try {
            const constraints = {
                video: {
                    deviceId: deviceId ? { exact: deviceId } : undefined
                },
                audio: true
            };

            const stream = await navigator.mediaDevices.getUserMedia(constraints);
            cameraFeed.srcObject = stream;
            currentStream = stream;

            statusDiv.textContent = `Camera "${cameraSelector.options[cameraSelector.selectedIndex].textContent}" started.`;
            updateButtonStates();

        } catch (err) {
            console.error('Error accessing camera:', err);
            statusDiv.textContent = `Error starting camera: ${err.name} - ${err.message}`;
            cameraFeed.srcObject = null;
            currentStream = null;
            updateButtonStates();
        }
    }

    /**
     * Stops the current video stream.
     */
    function stopCamera() {
        if (currentStream) {
            stopRecording();
            currentStream.getTracks().forEach(track => {
                track.stop();
            });
            cameraFeed.srcObject = null;
            currentStream = null;
            statusDiv.textContent = 'Camera stopped.';
        }
        updateButtonStates();
    }

    /**
     * Starts video recording.
     */
    function startRecording() {
        if (!currentStream) {
            statusDiv.textContent = 'Error: No camera stream active to record.';
            return;
        }
        if (!window.MediaRecorder) {
            statusDiv.textContent = 'Error: MediaRecorder API not supported in this browser.';
            return;
        }

        recordedChunks = []; // Clear previous recordings
        let options = {}; // Initialize options object

        // --- Determine the best MIME type for recording ---
        const preferredMp4Mime = 'video/mp4; codecs=avc1.42E01E,mp4a.40.2'; // H.264 Baseline, AAC-LC
        const fallbackWebmMime = 'video/webm; codecs=vp8,opus'; // VP8 video, Opus audio
        const genericWebmMime = 'video/webm';

        if (MediaRecorder.isTypeSupported(preferredMp4Mime)) {
            options.mimeType = preferredMp4Mime;
            currentRecordingMimeType = 'video/mp4';
            console.log(`Using ${preferredMp4Mime} for recording.`);
        } else if (MediaRecorder.isTypeSupported(fallbackWebmMime)) {
            options.mimeType = fallbackWebmMime;
            currentRecordingMimeType = 'video/webm';
            console.log(`Using ${fallbackWebmMime} for recording (MP4 not fully supported).`);
        } else if (MediaRecorder.isTypeSupported(genericWebmMime)) {
            options.mimeType = genericWebmMime;
            currentRecordingMimeType = 'video/webm';
            console.log(`Using generic ${genericWebmMime} for recording.`);
        } else {
            // Fallback to letting the browser choose if no specific type is supported
            // This might still fail if no recording types are available.
            delete options.mimeType;
            currentRecordingMimeType = 'application/octet-stream'; // Unknown/browser default
            console.warn('No specific video MIME type supported, letting browser choose.');
        }

        try {
            mediaRecorder = new MediaRecorder(currentStream, options);

            mediaRecorder.ondataavailable = (event) => {
                if (event.data.size > 0) {
                    recordedChunks.push(event.data);
                }
            };

            mediaRecorder.onstop = () => {
                isRecording = false;
                statusDiv.textContent = 'Recording stopped. Ready for download.';
                updateButtonStates();
            };

            mediaRecorder.onerror = (event) => {
                console.error('MediaRecorder error:', event.error);
                statusDiv.textContent = `Recording error: ${event.error.name || event.error}`;
                isRecording = false;
                updateButtonStates();
            };

            mediaRecorder.start();
            isRecording = true;
            statusDiv.textContent = 'Recording...';
            updateButtonStates();
            console.log('Recording started.');

        } catch (err) {
            console.error('Failed to create MediaRecorder:', err);
            statusDiv.textContent = `Error starting recording: ${err.name || err.message}`;
            isRecording = false;
            updateButtonStates();
        }
    }

    /**
     * Stops video recording.
     */
    function stopRecording() {
        if (isRecording && mediaRecorder && mediaRecorder.state === 'recording') {
            mediaRecorder.stop();
            console.log('Recording requested to stop.');
        } else {
            statusDiv.textContent = 'Not currently recording.';
            updateButtonStates();
        }
    }

    /**
     * Downloads the recorded video.
     */
    function downloadRecording() {
        if (recordedChunks.length === 0) {
            statusDiv.textContent = 'No recorded video to download.';
            return;
        }

        // Use the MIME type determined at the start of recording
        const blob = new Blob(recordedChunks, { type: currentRecordingMimeType });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        document.body.appendChild(a);
        a.style.display = 'none';
        a.href = url;

        // Set filename with appropriate extension
        const fileExtension = currentRecordingMimeType.includes('mp4') ? 'mp4' : 'webm';
        a.download = `recorded-video-${new Date().toISOString().slice(0,16).replace('T', '_').replace(':', '-')}.${fileExtension}`;
        a.click();

        window.URL.revokeObjectURL(url);
        console.log('Video download initiated.');

        recordedChunks = []; // Clear chunks after download
        updateButtonStates();
        statusDiv.textContent = `Video downloaded as .${fileExtension}.`;
    }

    /**
     * Toggles the CSS mirror effect on the video feed.
     * @param {boolean} isChecked Whether the mirror checkbox is checked.
     */
    function toggleMirror(isChecked) {
        cameraFeed.classList.toggle('mirrored', isChecked);
    }

    // --- Public methods exposed to HTMX ---
    return {
        startCameraClicked: function() {
            if (selectedDeviceId) {
                startCamera(selectedDeviceId);
            } else {
                statusDiv.textContent = 'Please select a camera first.';
            }
        },
        stopCamera: stopCamera,
        handleCameraChange: function(newDeviceId) {
            selectedDeviceId = newDeviceId;
            if (currentStream) {
                this.startCameraClicked();
            }
        },
        toggleMirror: toggleMirror,
        startRecordingClicked: startRecording,
        stopRecordingClicked: stopRecording,
        downloadRecordingClicked: downloadRecording,
        init: function() {
            populateCameraSelector();
            toggleMirror(mirrorCheckbox.checked);
            updateButtonStates();
        }
    };
})();

// --- Barcode Scanner Logic (separate from cameraApp - no changes here) ---
const lastScanValueElement = document.getElementById('lastScanValue');
const scanStatusElement = document.getElementById('scanStatus');

let barcodeBuffer = '';
let barcodeCharTimer;
const CHAR_TIMEOUT_MS = 100;

let canAcceptNextScan = true;
const SCAN_DELAY_MS = 500;
let scanDelayTimer;

function processBarcodeClientSide(scannedValue) {
    console.log('Barcode Scanned:', scannedValue);
    lastScanValueElement.textContent = scannedValue;
    lastScanValueElement.style.color = '#333';
}

function setScanStatus(message, type) {
    scanStatusElement.textContent = message;
    scanStatusElement.classList.remove('active-scan-status', 'debounce-status');
    if (type === 'ready') {
        scanStatusElement.classList.add('active-scan-status');
    } else if (type === 'waiting') {
        scanStatusElement.classList.add('debounce-status');
    }
}

document.addEventListener('keydown', (event) => {
    clearTimeout(barcodeCharTimer);
    barcodeCharTimer = setTimeout(() => {
        barcodeBuffer = '';
        console.log('Barcode buffer reset due to character timeout.');
    }, CHAR_TIMEOUT_MS);

    if (event.key === 'Enter' || event.keyCode === 13) {
        event.preventDefault();

        if (canAcceptNextScan) {
            const scannedValue = barcodeBuffer.trim();
            barcodeBuffer = '';

            if (scannedValue) {
                processBarcodeClientSide(scannedValue);

                canAcceptNextScan = false;
                setScanStatus(`Scan received. Waiting ${SCAN_DELAY_MS}ms for next scan...`, 'waiting');

                scanDelayTimer = setTimeout(() => {
                    canAcceptNextScan = true;
                    setScanStatus('Ready to scan.', 'ready');
                    console.log('Scan acceptance delay finished. Ready for next scan.');
                }, SCAN_DELAY_MS);

            } else {
                console.log('Enter key pressed, but barcode buffer was empty.');
            }
        } else {
            console.log('Scan ignored: still within debounce period.');
        }
    } else {
        if (event.key.length === 1 && !event.ctrlKey && !event.altKey && !event.metaKey) {
            barcodeBuffer += event.key;
        }
    }
});

// --- Overall Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    cameraApp.init();
    document.body.focus();
    setScanStatus('Ready to scan.', 'ready');
    console.log('Invisible barcode scanner initialized. Start scanning!');
});