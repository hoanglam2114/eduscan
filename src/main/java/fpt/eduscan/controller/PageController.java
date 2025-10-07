package fpt.eduscan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    /**
     * Home page - Landing page
     * Route: /home or /
     */
    @GetMapping({"/", "/home"})
    public String home() {
        return "index.html";
    }

    /**
     * Scan page - OCR scanning interface
     * Route: /scan
     */
    @GetMapping("/scan")
    public String scan() {
        return "scan.html";
    }

    /**
     * History page - View scan history
     * Route: /history
     */
    @GetMapping("/history")
    public String history() {
        return "history.html";
    }
}