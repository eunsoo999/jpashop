package jpabook.jpashop.Controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j // Log 사용(lombok)
public class HomeController {
    // Log : slf4j 사용
    // Logger log = LoggerFactory.getLogger(getClass());

    @RequestMapping("/")
    public String home() {
        log.info("home controller");
        return "home";
    }
}
