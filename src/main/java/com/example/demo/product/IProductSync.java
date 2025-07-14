package com.example.demo.product;

import com.example.demo.order.internal.Platform;
import com.example.demo.product.internal.CommonParameter;

public interface IProductSync {


    void productSync(Platform platform, CommonParameter commonParameter);

}
