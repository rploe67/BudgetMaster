package de.deadlocker8.budgetmaster.authentication;

import de.deadlocker8.budgetmaster.controller.BaseController;
import org.joda.time.DateTime;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class LoginController extends BaseController
{
	@RequestMapping("/login")
	public String login(HttpServletRequest request, Model model)
	{
		Map<String, String[]> paramMap = request.getParameterMap();

		if(paramMap.containsKey("error"))
			model.addAttribute("isError", true);

		if(paramMap.containsKey("logout"))
			model.addAttribute("isLogout", true);

		DefaultSavedRequest savedRequest = (DefaultSavedRequest)request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
		if(savedRequest != null)
		{
			request.getSession().setAttribute("preLoginURL", savedRequest.getRequestURL());
		}

		model.addAttribute("currentDate", DateTime.now());
		return "login";
	}
}