package com.shanzhu.blog.cms.service.impl;

import com.shanzhu.blog.cms.domain.CmsBlog;
import com.shanzhu.blog.cms.domain.CmsComment;
import com.shanzhu.blog.cms.domain.CmsCommentLike;
import com.shanzhu.blog.cms.mapper.CmsBlogMapper;
import com.shanzhu.blog.cms.mapper.CmsCommentLikeMapper;
import com.shanzhu.blog.cms.mapper.CmsCommentMapper;
import com.shanzhu.blog.cms.service.ICmsCommentService;
import com.shanzhu.blog.common.core.domain.entity.SysUser;
import com.shanzhu.blog.common.utils.DateUtils;
import com.shanzhu.blog.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * 评论管理Service业务层处理
 *
 * @author: ShanZhu
 * @date: 2023-12-09
 */
@Service
public class CmsCommentServiceImpl implements ICmsCommentService {

    @Resource
    private CmsCommentMapper cmsCommentMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private CmsCommentLikeMapper cmsCommentLikeMapper;

    @Resource
    private CmsBlogMapper cmsBlogMapper;

    /**
     * 首页查询评论列表
     */
    @Override
    public List<CmsComment> selectCommentList(CmsComment cmsComment) {
        //判断是否登录
        Long logUserUserId = null;
        String createBy = cmsComment.getCreateBy();
        if (createBy != null && !"".equals(createBy)) {
            SysUser logUser = sysUserMapper.selectUserByUserName(createBy);
            logUserUserId = logUser.getUserId();
        }
        cmsComment.setCreateBy(null);
        cmsComment.setType("0");
        cmsComment.setDelFlag("0");
        List<CmsComment> cmsCommentList = cmsCommentMapper.selectCmsCommentList(cmsComment);
        for (CmsComment comment : cmsCommentList) {
            //添加头像
            Long userId = comment.getUserId();
            if (userId != null) {
                SysUser user = sysUserMapper.selectUserById(userId);
                comment.setAvatar(user.getAvatar());
            }
            //添加是否被点赞
            if (logUserUserId != null) {
                CmsCommentLike commentLike = new CmsCommentLike();
                commentLike.setCommentId(comment.getId());
                commentLike.setUserId(logUserUserId);
                List<CmsCommentLike> likeList = cmsCommentLikeMapper.selectCmsCommentLikeList(commentLike);
                if (likeList.size() > 0) {
                    comment.setIsLike(true);
                } else {
                    comment.setIsLike(false);
                }
            }
            //添加子评论(回复)
            CmsComment childComment = new CmsComment();
            childComment.setType("1");
            childComment.setMainId(comment.getId());
            List<CmsComment> childCommentList = cmsCommentMapper.selectChildCommentList(childComment);
            if (childCommentList.size() > 0) {
                for (CmsComment childListComment : childCommentList) {
                    //添加头像
                    Long childUserId = childListComment.getUserId();
                    if (childUserId != null) {
                        SysUser user = sysUserMapper.selectUserById(childUserId);
                        childListComment.setAvatar(user.getAvatar());
                    }
                    //添加是否被点赞
                    if (logUserUserId != null) {
                        CmsCommentLike commentLike = new CmsCommentLike();
                        commentLike.setCommentId(comment.getId());
                        commentLike.setUserId(logUserUserId);
                        List<CmsCommentLike> likeList = cmsCommentLikeMapper.selectCmsCommentLikeList(commentLike);
                        if (likeList.size() > 0) {
                            comment.setIsLike(true);
                        } else {
                            comment.setIsLike(false);
                        }
                    }
                    //添加父评论信息
                    CmsComment byId = cmsCommentMapper.selectCmsCommentById(childListComment.getParentId());
                    childListComment.setPCreateBy(byId.getCreateBy());
                }
                comment.setChildren(childCommentList);
            }
        }
        return cmsCommentList;
    }

    @Override
    public int addCmsCommentLike(CmsCommentLike cmsCommentLike) {
        int result = -1;
        String createBy = cmsCommentLike.getCreateBy();
        if (!"".equals(createBy) && createBy != null) {
            SysUser user = sysUserMapper.selectUserByUserName(createBy);
            if (user != null) {
                cmsCommentLike.setUserId(user.getUserId());
                cmsCommentLikeMapper.addCmsCommentLike(cmsCommentLike);
            }
        }
        //修改点赞数量
        CmsComment cmsComment = new CmsComment();
        cmsComment.setId(cmsCommentLike.getCommentId());
        cmsComment.setLikeNum(cmsCommentLike.getLikeNum());
        result = cmsCommentMapper.updateCmsComment(cmsComment);
        return result;
    }

    @Override
    public int delCmsCommentLike(CmsCommentLike cmsCommentLike) {
        int result = -1;
        String createBy = cmsCommentLike.getCreateBy();
        if (!"".equals(createBy) && createBy != null) {
            SysUser user = sysUserMapper.selectUserByUserName(createBy);
            if (user != null) {
                cmsCommentLike.setUserId(user.getUserId());
                cmsCommentLikeMapper.deleteCmsCommentLike(cmsCommentLike);
            }
        }
        //修改点赞数量
        CmsComment cmsComment = new CmsComment();
        cmsComment.setId(cmsCommentLike.getCommentId());
        cmsComment.setLikeNum(cmsCommentLike.getLikeNum());
        result = cmsCommentMapper.updateCmsComment(cmsComment);
        return result;
    }

    /**
     * 查询评论管理
     *
     * @param id 评论管理主键
     * @return 评论管理
     */
    @Override
    public CmsComment selectCmsCommentById(Long id) {
        return cmsCommentMapper.selectCmsCommentById(id);
    }

    /**
     * 查询评论管理列表
     *
     * @param cmsComment 评论管理
     * @return 评论管理
     */
    @Override
    public List<CmsComment> selectCmsCommentList(CmsComment cmsComment) {
        List<CmsComment> cmsCommentList = new ArrayList<>();
        //判断用户权限
        String createBy = cmsComment.getCreateBy();
        if (createBy != null && !"".equals(createBy)) {
            SysUser user = sysUserMapper.selectUserByUserName(createBy);
            if (user != null) {
                List<CmsComment> CommentList = cmsCommentMapper.selectCmsCommentList(cmsComment);
                for (CmsComment comment : CommentList) {
                    //查询子评论(回复)
                    CmsComment childComment = new CmsComment();
                    childComment.setType("1");
                    childComment.setParentId(comment.getId());
                    List<CmsComment> childCommentList = cmsCommentMapper.selectCmsCommentList(childComment);
                    if (childCommentList.size() > 0) {
                        cmsCommentList.addAll(childCommentList);
                    }
                }
                cmsCommentList.addAll(CommentList);
            }
        } else {
            cmsCommentList = cmsCommentMapper.selectCmsCommentList(cmsComment);
        }
        for (CmsComment comment : cmsCommentList) {
            //添加头像
            Long userId = comment.getUserId();
            if (userId != null) {
                SysUser user = sysUserMapper.selectUserById(userId);
                comment.setAvatar(user.getAvatar());
            }
            //添加父评论信息
            Long parentId = comment.getParentId();
            if (parentId != null) {
                CmsComment parentComment = cmsCommentMapper.selectCmsCommentById(parentId);
                comment.setPCreateBy(parentComment.getCreateBy());
            }
            //添加博客信息
            Long blogId = comment.getBlogId();
            if (blogId != null) {
                CmsBlog blog = cmsBlogMapper.selectCmsBlogById(blogId);
                if(blog != null){
                    comment.setBlogTitle(blog.getTitle());
                }
            }
        }
        //排序
//        String[] sortNameArr1 = {"createTime"};
//        //true升序，false降序
//        boolean[] isAscArr1 = {false};
//        ListSortUtils.sort(cmsMessageList, sortNameArr1, isAscArr1);
//        cmsMessageList.sort((a,b)->a.getCreateBy().compareTo(b.getCreateBy()));
//        Collections.sort(cmsMessageList, new Comparator<CmsMessage>() {
//            @Override
//            public int compare(CmsMessage o1, CmsMessage o2) {
//                //升序
//                //return o1.getCreateBy().compareTo(o2.getCreateBy());
//                //降序
//                return o2.getCreateBy().compareTo(o1.getCreateBy());
//            }
//        });
        return cmsCommentList;
    }

    /**
     * 新增评论管理
     *
     * @param cmsComment 评论管理
     * @return 结果
     */
    @Override
    public int insertCmsComment(CmsComment cmsComment) {
        String createBy = cmsComment.getCreateBy();
        if (createBy != null && !"".equals(createBy)) {
            SysUser user = sysUserMapper.selectUserByUserName(createBy);
            if (user != null) {
                cmsComment.setUserId(user.getUserId());
            }
        }
        cmsComment.setCreateTime(DateUtils.getNowDate());
        return cmsCommentMapper.insertCmsComment(cmsComment);
    }

    /**
     * 修改评论管理
     *
     * @param cmsComment 评论管理
     * @return 结果
     */
    @Override
    public int updateCmsComment(CmsComment cmsComment) {
        cmsComment.setUpdateTime(DateUtils.getNowDate());
        return cmsCommentMapper.updateCmsComment(cmsComment);
    }

    /**
     * 批量删除评论管理
     *
     * @param ids 需要删除的评论管理主键
     * @return 结果
     */
    @Override
    public int deleteCmsCommentByIds(Long[] ids) {
        return cmsCommentMapper.updateDelFlagByIds(ids);
    }

    /**
     * 删除评论管理信息
     *
     * @param id 评论管理主键
     * @return 结果
     */
    @Override
    public int deleteCmsCommentById(Long id) {
        return cmsCommentMapper.updateDelFlagById(id);
    }
}
