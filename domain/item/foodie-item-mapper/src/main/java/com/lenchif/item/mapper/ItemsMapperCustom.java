package com.lenchif.item.mapper;


import com.lenchif.item.pojo.vo.ItemCommentVO;
import com.lenchif.item.pojo.vo.ShopcartVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ItemsMapperCustom {
    public List<ItemCommentVO> queryItemComments(@Param("paramsMap") Map<String, Object> map);

//    public List<SearchItemsVO> searchItems (@Param("paramsMap") Map<String, Object> map);
//
//    public List<SearchItemsVO> searchItemsByThirdCat (@Param("paramsMap") Map<String, Object> map);

    public List<ShopcartVO> queryItemsBySpecIds (@Param("paramsList") List specIdsList);

    public int decreaseItemSpecStock(@Param("specId") String specId,
                                     @Param("pendingCounts") int pendingCounts);


}