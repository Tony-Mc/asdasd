package web.controllers;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.thymeleaf.context.WebContext;
import web.helpers.Controller;
import web.helpers.PathMapping;
import web.helpers.Sender;

@Controller("Form")
public class FormController {

    @PathMapping("")
    public void submit(WebContext ctx) throws Exception {

        Subject sub = SecurityUtils.getSubject();
        if (!sub.hasRole("potentialCandidate")) {
            ctx.getResponse().sendRedirect(ctx.getResponse().encodeRedirectURL("/app"));
            return;
        }
        ctx.setVariable("pageTitle", "Form");
        ctx.setVariable("layout","shared/_noFooterLayout");
        Sender.sendView(ctx, "auth/form");
    }
}
