package com.lenchif.order.pojo.bo;

import com.lenchif.pojo.ShopcartBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by 半仙.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderBO {

    private SubmitOrderBO order;

    private List<ShopcartBO> items;

}
