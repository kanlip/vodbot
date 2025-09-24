#!/bin/bash

# Create S3 bucket for Vodbot video uploads
awslocal s3api create-bucket --bucket vodbot-videos --region us-east-1

# Enable versioning on the bucket
awslocal s3api put-bucket-versioning --bucket vodbot-videos --versioning-configuration Status=Enabled

# Create bucket for development
awslocal s3api create-bucket --bucket vodbot-videos-dev --region us-east-1

echo "S3 buckets created successfully for Vodbot"
