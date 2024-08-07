package com.example.geungeunhanjan.controller.user;

import com.example.geungeunhanjan.domain.dto.user.UserSessionDTO;
import com.example.geungeunhanjan.domain.vo.user.UniVO;
import com.example.geungeunhanjan.domain.vo.user.UserVO;
import com.example.geungeunhanjan.mapper.user.UserMapper;
import com.example.geungeunhanjan.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    //로그인 회원가입 관리하는 페이지 입니다!!!!!!!!!!!!!!!!!!!!!!!

    private final UserService userService;
    private final UserMapper userMapper;


    @GetMapping("/join")
    public String join() {
        return "/user/join";
    }

    // 기능처리
    @PostMapping("/join")
    public String join(HttpServletRequest request) {
        UserVO userVO = new UserVO();
        String year = request.getParameter("userBirth");
        String month = request.getParameter("month");
        String day = request.getParameter("day");

        String formattedDate = String.format("%s-%02d-%02d", year, Integer.parseInt(month), Integer.parseInt(day));
        LocalDate birthDate = LocalDate.parse(formattedDate);

        userVO.setUserId(userMapper.getUserSeqNext());
        userVO.setUserName(request.getParameter("userName"));
        userVO.setUserPassword(request.getParameter("userPassword"));
        userVO.setUserPhone(request.getParameter("userPhone"));
        userVO.setUserEmail(request.getParameter("userEmail"));
        userVO.setUserNickname(request.getParameter("userNickname"));
        userVO.setUserBirth(birthDate.atStartOfDay());

        userMapper.userJoin(userVO);

        UniVO uniVO = new UniVO();
        uniVO.setUserId(userVO.getUserId());
        uniVO.setUserBirth(birthDate.atStartOfDay());

        userService.userUniJoin(uniVO);


        return "redirect:/user/login";
    }

    // 로그인 getMapping
    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        if (model.containsAttribute("loginError")) {
            System.out.println("loginError: " + model.getAttribute("loginError"));
        }
        return "/user/login";
    }


    @PostMapping("/login")
    public String userLogin(@RequestParam("userEmail") String userEmail,
                            @RequestParam("userPassword") String userPassword,
                            HttpSession session,
                            Model model) {

        Long userId = userService.userLogin(userEmail, userPassword);
        boolean result = userId != null && userId > 0;


        if (result) {
            UserSessionDTO userSessionDTO = userService.uniUserIdNickname(userId);
            session.setAttribute("uniId", userSessionDTO.getUniId());
            session.setAttribute("userNickname", userSessionDTO.getUserNickname());
            return "redirect:/main";
        } else {
            model.addAttribute("loginError", "잘못된 정보입니다. 다시 로그인하세요.");
            return "/user/login";
        }
    }


    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 세션 무효화
        return "redirect:/main";
    }

    @GetMapping("/login/passwordFind")
    public String findPassword() {
        return "user/passwordFind";
    }


    //인증페이지

    @GetMapping("/login/passwordFind/passwordFind2")
    public String findPassword2() {
        return "user/passwordFind2";
    }

    @PostMapping("/login/passwordFind/passwordFind2")
    public String findPassword2(@RequestParam("userEmail") String userEmail,
                                @RequestParam("certiNumber") String certiNumber,
                                RedirectAttributes redirectAttributes,
                                Model model){
        System.out.println(userEmail);
        System.out.println(certiNumber);

        if(userService.selectCerti(userEmail).getCertiNumber().equals(certiNumber)){
            redirectAttributes.addAttribute("userEmail", userEmail);
            return "redirect:/user/login/changePassword";
        }
        model.addAttribute("errorMessage", "인증번호가 틀렸습니다.");
        return "user/passwordFind2";
    }


    //비밀번호 변경페이지

    @GetMapping("/login/changePassword")
    public String changePassword(@RequestParam("userEmail") String userEmail, Model model){
        model.addAttribute("userEmail", userEmail);
        return "user/changePassword";
    }

    @PostMapping("/login/changePassword")
    public String changePassword(@RequestParam("userEmail") String userEmail, @RequestParam("userPassword") String userPassword){
        userService.updatePassword(userPassword, userEmail);
        return "redirect:/user/login";
    }



//    private final OAuth2AuthorizedClientService authorizedClientService;
//    private final CustomOAuth2UserService customOAuth2UserService;
//
//    @GetMapping("/login/oauth2/code/kakao")
//    public String kakaoLoginCallback(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
//
//        // OAuth2User 객체에서 필요한 사용자 정보를 가져와서 처리
//        String name = customOAuth2User.getName();
//        String profilePic = customOAuth2User.getProfilePic();
//        String providerId = customOAuth2User.getProviderId();
////        String name = (String) oauth2User.getAttribute("name");
////        String profilePic = (String) oauth2User.getAttribute("profile_image");
////        String providerId = (String) oauth2User.getAttribute("id");
//
//        KakaoVO kakaoUser = kakaoUserService.findByProviderId(providerId);
//        if (kakaoUser == null) {
//            // 새로운 사용자라면 DB에 저장
//            kakaoUser = new KakaoVO();
//            kakaoUser.setName(name);
//            kakaoUser.setProfilePic(profilePic);
////            kakaoUser.setProvider(provider);
//            kakaoUser.setProviderId(providerId);
//            kakaoUserService.insertUser(kakaoUser);
//        } else {
//            // 기존 사용자라면 정보 업데이트
//            kakaoUser.setName(name);
//            kakaoUser.setProfilePic(profilePic);
//            kakaoUserService.updateUser(kakaoUser);
//        }
//
//        return "redirect:/main"; // 로그인 후 리디렉션할 경로
//    }

}
