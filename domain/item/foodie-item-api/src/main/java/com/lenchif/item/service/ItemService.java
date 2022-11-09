package com.lenchif.item.service;

import com.lenchif.item.pojo.Items;
import com.lenchif.item.pojo.ItemsImg;
import com.lenchif.item.pojo.ItemsParam;
import com.lenchif.item.pojo.ItemsSpec;
import com.lenchif.item.pojo.vo.CommentLevelCountsVO;
import com.lenchif.item.pojo.vo.ShopcartVO;
import com.lenchif.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient("foodie-item-service")
@RequestMapping("item-api")
public interface ItemService {

    @GetMapping("item")
    public Items queryItemById(@RequestParam("itemId") String itemId);

    @GetMapping("itemImages")
    public List<ItemsImg> queryItemImgList(@RequestParam("itemId") String itemId);

    @GetMapping("itemSpecs")
    public List<ItemsSpec> queryItemSpecList(@RequestParam("itemId") String itemId);

    @GetMapping("itemParam")
    public ItemsParam queryItemParam (@RequestParam("itemId") String itemId);

    @GetMapping("countComments")
    public CommentLevelCountsVO queryCommentCounts(@RequestParam("itemId") String itemId);

    @GetMapping("pagedComments")
    public PagedGridResult queryPagedComments(@RequestParam("itemId") String itemId,
                                              @RequestParam(value = "level", required = false)Integer level,
                                              @RequestParam(value = "page", required = false)Integer page,
                                              @RequestParam(value = "pageSize", required = false)Integer pageSize);

//    public PagedGridResult searhItems(String keywords, String sort,
//                      Integer page, Integer pageSize);
//
//    public PagedGridResult searhItems(Integer catId, String sort,
//                                      Integer page, Integer pageSize);

    @GetMapping("getCartBySpecIds")
    public List<ShopcartVO> queryItemsBySpecIds(@RequestParam("specIds")String specIds);

    @GetMapping("singleItemSpec")
    public ItemsSpec queryItemSpecById(@RequestParam("specId")String specId);

    @GetMapping("primaryImage")
    public String queryItemMainImgById(@RequestParam("itemId")String itemId);

    @PostMapping("decreaseStock")
    public void decreaseItemSpecStock(@RequestParam("specId")String specId,@RequestParam("buyCounts") int buyCounts);
}
