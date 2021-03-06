package com.online.college.portal.controller;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.online.college.common.page.TailPage;
import com.online.college.common.storage.QiniuStorage;
import com.online.college.common.util.EncryptUtil;
import com.online.college.common.web.JsonView;
import com.online.college.common.web.SessionContext;
import com.online.college.core.auth.domain.AuthUser;
import com.online.college.core.auth.service.IAuthUserService;
import com.online.college.core.course.domain.CourseComment;
import com.online.college.core.course.service.ICourseCommentService;
import com.online.college.core.user.domain.UserCollections;
import com.online.college.core.user.domain.UserCourseSection;
import com.online.college.core.user.domain.UserCourseSectionDto;
import com.online.college.core.user.domain.UserFollowStudyRecord;
import com.online.college.core.user.service.IUserCollectionsService;
import com.online.college.core.user.service.IUserCourseSectionService;


@Controller
@RequestMapping("/user")
public class UserController {
	

	
	@Autowired
	private IAuthUserService authUserService;
	
	@Autowired
	private IUserCourseSectionService userCourseSectionService;
	
	@Autowired
	private IUserCollectionsService userCollectionsService;
	
	@Autowired
	private ICourseCommentService courseCommentService;
	

	
	/**
	 * 我的课程
	 */
	@RequestMapping("/course")
	public ModelAndView course(TailPage<UserCourseSectionDto> page){
		ModelAndView mv = new ModelAndView("user/course");
		mv.addObject("curNav","course");
		
		UserCourseSection queryEntity = new UserCourseSection();
		queryEntity.setUserId(SessionContext.getUserId());
		page = userCourseSectionService.queryPage(queryEntity, page);
		mv.addObject("page", page);
		
		return mv;
	}
	
	/**
	 * 我的收藏
	 */
	@RequestMapping("/collect")
	public ModelAndView collect(TailPage<UserCollections> page){
		ModelAndView mv = new ModelAndView("user/collect");
		mv.addObject("curNav","collect");
		UserCollections queryEntity = new UserCollections();
		queryEntity.setUserId(SessionContext.getUserId());
		page = userCollectionsService.queryPage(queryEntity, page);
		
		mv.addObject("page", page);
		return mv;
	}
	
	/**
	 * 信息
	 */
	@RequestMapping("/info")
	public ModelAndView info(){
		ModelAndView mv = new ModelAndView("user/info");
		mv.addObject("curNav","info");
		
		AuthUser authUser = authUserService.getById(SessionContext.getUserId());
		if(null != authUser && StringUtils.isNotEmpty(authUser.getHeader())){
			authUser.setHeader(QiniuStorage.getUrl(authUser.getHeader()));
		}
		mv.addObject("authUser",authUser);
		return mv;
	}
	/**
	 * 保存信息
	 */
	@RequestMapping("/saveInfo")
	@ResponseBody
	public String saveInfo(AuthUser authUser, @RequestParam MultipartFile pictureImg){
		try {
			authUser.setId(SessionContext.getUserId());
			if (null != pictureImg && pictureImg.getBytes().length > 0) {
				String key = QiniuStorage.uploadImage(pictureImg.getBytes());
				authUser.setHeader(key);
			}
			authUserService.updateSelectivity(authUser);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new JsonView().toString();
	}
	
	/**
	 * 密码
	 */
	@RequestMapping("/passwd")
	public ModelAndView passwd(){
		ModelAndView mv = new ModelAndView("user/passwd");
		mv.addObject("curNav","passwd");
		return mv;
	}
	
	/**
	 * 密码
	 */
	@RequestMapping("/savePasswd")
	@ResponseBody
	public String savePasswd(String oldPassword, String password, String rePassword){
		AuthUser currentUser = authUserService.getById(SessionContext.getUserId());
		if(null == currentUser){
			return JsonView.render(1,"用户不存在！");
		}
		oldPassword = EncryptUtil.encodedByMD5(oldPassword.trim());
		if(!oldPassword.equals(currentUser.getPassword())){
			return JsonView.render(1,"旧密码不正确！");
		}
		if(StringUtils.isEmpty(password.trim())){
			return JsonView.render(1,"新密码不能为空！");
		}
		if(!password.trim().equals(rePassword.trim())){
			return JsonView.render(1,"新密码与重复密码不一致！");
		}
		currentUser.setPassword(EncryptUtil.encodedByMD5(password));
		authUserService.updateSelectivity(currentUser);
		return new JsonView().toString();
	}
	

	
}
