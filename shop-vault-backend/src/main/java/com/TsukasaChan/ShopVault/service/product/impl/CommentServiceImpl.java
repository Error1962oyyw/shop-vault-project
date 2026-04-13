package com.TsukasaChan.ShopVault.service.product.impl;

import com.TsukasaChan.ShopVault.common.EntityConstants;
import com.TsukasaChan.ShopVault.common.QueryHelper;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.entity.product.Comment;
import com.TsukasaChan.ShopVault.entity.system.MessagePush;
import com.TsukasaChan.ShopVault.mapper.product.CommentMapper;
import com.TsukasaChan.ShopVault.service.order.OrderService;
import com.TsukasaChan.ShopVault.service.product.CommentService;
import com.TsukasaChan.ShopVault.service.system.MessagePushService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final OrderService orderService;
    private final MessagePushService messagePushService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(Comment comment, Long userId) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, comment.getOrderId())
                .eq(Order::getUserId, userId));

        if (order == null) {
            throw new RuntimeException("非法操作：未找到该订单");
        }
        if (order.getStatus() != 3) {
            throw new RuntimeException("必须在确认收货后才能评价！");
        }

        long count = this.count(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getOrderId, comment.getOrderId())
                .eq(Comment::getProductId, comment.getProductId()));
        if (count > 0) {
            throw new RuntimeException("您已经评价过该商品，请勿重复提交");
        }

        comment.setUserId(userId);
        comment.setAuditStatus(0);
        this.save(comment);

        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        messagePushService.pushToAdmin(
                MessagePush.TYPE_ADMIN_COMMENT,
                "商品评价通知",
                "[" + timeStr + "] 收到一条商品评价通知消息",
                "/admin/comments",
                comment.getId()
        );
    }

    @Override
    public void likeComment(Long commentId) {
        Comment comment = this.getById(commentId);
        if (comment != null) {
            comment.setLikes(comment.getLikes() + 1);
            this.updateById(comment);
        }
    }

    @Override
    public void reportComment(Long commentId) {
        Comment comment = this.getById(commentId);
        if (comment != null) {
            comment.setIsReported(1);
            this.updateById(comment);
        }
    }

    @Override
    public void adminDeleteComment(Long commentId) {
        Comment comment = this.getById(commentId);
        if (comment != null) {
            comment.setAuditStatus(2);
            this.updateById(comment);
        }
    }

    @Override
    public List<Comment> getCommentsByProductId(Long productId) {
        return this.list(QueryHelper.build(wrapper -> wrapper
                .eq(Comment::getProductId, productId)
                .eq(Comment::getAuditStatus, EntityConstants.Status.ENABLED)
                .orderByDesc(Comment::getLikes)
                .orderByDesc(Comment::getCreateTime)));
    }

    @Override
    public IPage<Comment> getCommentsByProductIdPaged(Long productId, Integer pageNum, Integer pageSize) {
        Page<Comment> page = QueryHelper.createPage(pageNum, pageSize);
        return this.page(page, QueryHelper.build(wrapper -> wrapper
                .eq(Comment::getProductId, productId)
                .eq(Comment::getAuditStatus, EntityConstants.Status.ENABLED)
                .orderByDesc(Comment::getLikes)
                .orderByDesc(Comment::getCreateTime)));
    }

    @Override
    public IPage<Comment> getCommentPage(Integer pageNum, Integer pageSize) {
        Page<Comment> page = QueryHelper.createPage(pageNum, pageSize);
        return this.page(page, QueryHelper.build(wrapper -> wrapper
                .eq(Comment::getAuditStatus, EntityConstants.Status.ENABLED)
                .orderByDesc(Comment::getCreateTime)));
    }
}
