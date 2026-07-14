package com.example.app.controller;

import com.example.app.dto.ProductRequest;
import com.example.app.model.SeckillProduct;
import com.example.app.service.ProductService;
import com.example.app.common.ApiResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
@RestController
@RequestMapping("/api/products")
@Tag(name = "商品管理", description="秒杀商品的创建、查询、更新")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService=productService;
    }
    @Operation(summary="查询所有商品",description="查询所有秒杀商品")
    @GetMapping
    public ApiResponse<List<SeckillProduct>> findAll(){
        return ApiResponse.success(productService.getAllProducts());
    }
    @Operation(summary="用商品id查询商品",description="用商品id查询秒杀商品")
    @GetMapping("/{id}")
    public ApiResponse<SeckillProduct> getProductId(@PathVariable Long id){
        return ApiResponse.success(productService.getProductId(id));
    }
    @Operation(summary="用商品名字查询商品",description="用商品名字查询秒杀商品")
    @GetMapping("/name/{name}")
    public ApiResponse<SeckillProduct> getProductName(@PathVariable String name){
        return ApiResponse.success(productService.getProductName(name));
    }
    @Operation(summary = "创建商品", description = "新增秒杀商品")
    @PostMapping
    public ApiResponse<SeckillProduct> createProduct( @Valid @RequestBody ProductRequest request){
        return ApiResponse.success(productService.createProduct(request));
    }
    @Operation(summary="修改商品",description="修改秒杀商品")
    @PatchMapping("/{id}")
    public ApiResponse<SeckillProduct> updateProduct(@PathVariable Long id,@Valid @RequestBody ProductRequest request){
        return ApiResponse.success(productService.updateProduct(id,request));
    }
}
