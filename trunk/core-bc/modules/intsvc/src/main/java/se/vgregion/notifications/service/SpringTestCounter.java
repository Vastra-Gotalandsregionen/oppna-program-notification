package se.vgregion.notifications.service;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.HashMap;
import java.util.Random;

/**
 * @author Patrik Bergström
 */

@Controller
public class SpringTestCounter {

    @RequestMapping("/getCount")
    public ModelAndView getRandom() {
        String msg = get();
        System.out.println("GET: "+msg);
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("val", msg);
        return new ModelAndView("value", model);
    }

    private String get() {
        Random random = new Random();
        return random.nextInt(100) + "";
    }
}
