package io.kyligence.notebook.console.support;

import io.kyligence.notebook.console.bean.dto.Response;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.SneakyThrows;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@Controller
public class ErrorPageHandler implements ErrorController {


    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/error")
    @ResponseBody
    @SneakyThrows
    public void handlerError(HttpServletResponse response){
        Response<String> resp;
        int status = response.getStatus();
        if (status == 404) {
            resp = new Response<String>().data("Page not find");
        } else {
            resp = new Response<String>().data("Unknown error");
        }
        response.getWriter().write(JacksonUtils.writeJson(resp));
        response.getWriter().flush();
    }

}
