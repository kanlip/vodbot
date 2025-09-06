package com.example.demo.productmapping.adapter.in.web;

import com.example.demo.productmapping.domain.ProductMapping;
import com.example.demo.productmapping.port.in.ProductMappingUseCase;
import com.example.demo.shared.domain.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Web controller for Product Mapping UI.
 */
@Controller
@RequestMapping("/product-mappings")
@RequiredArgsConstructor
public class ProductMappingWebController {
    
    private final ProductMappingUseCase productMappingUseCase;
    
    @GetMapping
    public String listMappings(Model model) {
        model.addAttribute("mappings", productMappingUseCase.findAll());
        model.addAttribute("platforms", Platform.values());
        return "product-mappings/list";
    }
    
    @GetMapping("/seller/{sellerId}")
    public String listMappingsBySeller(@PathVariable String sellerId, Model model) {
        model.addAttribute("mappings", productMappingUseCase.findBySellerId(sellerId));
        model.addAttribute("platforms", Platform.values());
        model.addAttribute("sellerId", sellerId);
        return "product-mappings/list";
    }
    
    @GetMapping("/create")
    public String createMappingForm(Model model) {
        model.addAttribute("mapping", new ProductMapping());
        model.addAttribute("platforms", Platform.values());
        return "product-mappings/form";
    }
    
    @PostMapping("/create")
    public String createMapping(@ModelAttribute ProductMapping mapping) {
        try {
            productMappingUseCase.createMapping(mapping);
            return "redirect:/product-mappings?success=created";
        } catch (IllegalArgumentException e) {
            return "redirect:/product-mappings/create?error=" + e.getMessage();
        }
    }
    
    @GetMapping("/{id}/edit")
    public String editMappingForm(@PathVariable UUID id, Model model) {
        ProductMapping mapping = productMappingUseCase.findById(id);
        model.addAttribute("mapping", mapping);
        model.addAttribute("platforms", Platform.values());
        return "product-mappings/form";
    }
    
    @PostMapping("/{id}/edit")
    public String updateMapping(@PathVariable UUID id, @ModelAttribute ProductMapping mapping) {
        try {
            productMappingUseCase.updateMapping(id, mapping);
            return "redirect:/product-mappings?success=updated";
        } catch (IllegalArgumentException e) {
            return "redirect:/product-mappings/" + id + "/edit?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteMapping(@PathVariable UUID id) {
        productMappingUseCase.deleteMapping(id);
        return "redirect:/product-mappings?success=deleted";
    }
    
    @PostMapping("/{id}/deactivate")
    public String deactivateMapping(@PathVariable UUID id) {
        productMappingUseCase.deactivateMapping(id);
        return "redirect:/product-mappings?success=deactivated";
    }
    
    @PostMapping("/sync")
    @ResponseBody
    public String syncFromPlatform(
            @RequestParam String sellerId,
            @RequestParam Platform platform,
            @RequestParam String platformProductId,
            @RequestParam String productName) {
        try {
            ProductMapping mapping = productMappingUseCase.syncFromPlatform(sellerId, platform, platformProductId, productName);
            return "<div class='alert alert-success'>Product synced successfully: " + mapping.getSku() + "</div>";
        } catch (Exception e) {
            return "<div class='alert alert-danger'>Error syncing product: " + e.getMessage() + "</div>";
        }
    }
}