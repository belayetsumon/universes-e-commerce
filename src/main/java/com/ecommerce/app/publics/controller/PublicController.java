/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.publics.controller;

import com.ecommerce.app.model.BlogCategory;
import com.ecommerce.app.model.Contact;
import com.ecommerce.app.model.Gallery;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoginEventService;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.ripository.BlogCategoryRepository;
import com.ecommerce.app.ripository.BlogRepository;
import com.ecommerce.app.ripository.ContactRepository;
import com.ecommerce.app.ripository.FaqRepository;
import com.ecommerce.app.ripository.GalleryRepository;
import com.ecommerce.app.ripository.ImageGalleryRepository;
import com.ecommerce.app.ripository.NewsRepository;
import com.ecommerce.app.ripository.OurclientsRepository;
import com.ecommerce.app.ripository.OurservicesRepository;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.ripository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ecommerce.app.product.ripository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/public")
@PreAuthorize("permitAll()")
public class PublicController {

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    GalleryRepository galleryRepository;

    @Autowired
    ImageGalleryRepository imageGalleryRepository;

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    ProductcategoryRepository productcategoryRepository;



    @Autowired
    ProductRepository productRepository;

    @Autowired
    OurservicesRepository ourservicesRepository;

    @Autowired
    OurclientsRepository ourclientsRepository;

    @Autowired
    FaqRepository faqRepository;



    @Autowired
    ProductRepository examRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    BlogRepository blogRepository;

    @Autowired
    BlogCategoryRepository blogCategoryRepository;

    @Autowired
    LoginEventService loginEventService;

    @RequestMapping("/about-us")
    public String aboutUs(Model model) {
        model.addAttribute("attribute", "value");
        return "frontview/aboutUs";
    }

//    @RequestMapping("/mission-vision")
//    public String missionVvision(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/mission-vision";
//    }
//    @RequestMapping("/ceo-message")
//    public String ceomessage(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/ceo-message";
//    }
//    @RequestMapping("/agent-division")
//    public String agentdivision(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/agent_division";
//    }
//    @RequestMapping("/trade-division")
//    public String tradedivision(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/trade_division";
//    }
//    @RequestMapping("/corporate-value")
//    public String corporatevalue(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/corporate_value";
//    }
//    @RequestMapping("/code-conduct")
//    public String codeconduct(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/code_conduct";
//    }
//    @RequestMapping("/executive-committee")
//    public String executiveCommittee(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/executive-committee";
//    }
//    @RequestMapping("/constitution")
//    public String constitution(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/constitution";
//    }
//    @RequestMapping("/advisory-council")
//    public String advisoryCouncil(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/advisory-council";
//    }
//    @RequestMapping("/all-member")
//    public String allmember(Model model) {
//        model.addAttribute("allmember", usersRepository.findByStatus(Status.Active));
//        return "frontview/all-member";
//    }
//    @RequestMapping("/member-search")
//    public String membersearch(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/all-search";
//    }
    @RequestMapping("/member-login")
    public String memberlogin(Model model) {
        model.addAttribute("attribute", "value");

        return "frontview/member-login";
    }

    @RequestMapping("/front-registration")
    public String studentRegistration(Model model,
            Users users) {

        /////Role instructor = roleRepository.findBySlug("instructor");
        ////  model.addAttribute("instructor", instructor);
        //// Role customer = roleRepository.findBySlug("customer");
        ///// model.addAttribute("customer", customer);
        return "frontview/front-registration";
    }

    @RequestMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("attribute", "value");
        return "frontview/forgot-password";
    }

//    @RequestMapping("/batch-modaretor")
//    public String batchmodaretor(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/batch-modaretor";
//    }
//    @RequestMapping("/resourses")
//    public String resourses(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/resourses";
//    }
//    @RequestMapping("/wholesale")
//    public String wholesale(Model model) {
//        model.addAttribute("attribute", "value");
//        return "frontview/wholesale";
//    }
    @RequestMapping("/product")
    public String product(Model model) {

        model.addAttribute("productlist", productRepository.findByStatusOrderByIdDesc(ProductStatusEnum.Active));
        model.addAttribute("productcategorylist", productcategoryRepository.findByStatusAndParentIsNull(ProductStatusEnum.Active));
        return "frontview/product";
    }

    @RequestMapping("/product-by-category/{prodcatid}")
    public String productByCategory(Model model, @PathVariable long prodcatid, Productcategory productcategory) {

        productcategory.setId(prodcatid);

       // model.addAttribute("productlist", ourproductRepository.findByProductcategoryOrderByIdDesc(productcategory));

        //model.addAttribute("productcategorylist", productcategoryRepository.findByStatusAndOurproductStatus(com.ecommerce.app.model.enumvalue.Status.Active, com.ecommerce.app.model.enumvalue.Status.Active));

        model.addAttribute("productcategoryname", productcategoryRepository.getReferenceById(prodcatid));

        return "frontview/product-by-category";
    }

    @RequestMapping("/single-product/{prodid}")
    public String single_product(Model model, @PathVariable long prodid, Product product) {

        model.addAttribute("product_details", productRepository.getReferenceById(prodid));

        return "frontview/single-product";
    }

    @RequestMapping("/news-events")
    public String newsEvents(Model model, @RequestParam(defaultValue = "0") int page) {

//        Pageable pageable;
//
//        pageable = new PageRequest(page, 5, Sort.by("id").descending());
//
//        Page<News> pagelist = newsRepository.findAll(pageable);
//
//        model.addAttribute("pagelist", pagelist);
//
        return "frontview/newsEvents";
    }

    @RequestMapping("/news-details/{newsid}")
    public String newsdetails(Model model, @PathVariable Long newsid) {

        model.addAttribute("singlenews", newsRepository.getReferenceById(newsid));
        return "frontview/newsdetails";
    }

    @RequestMapping("/gallery")
    public String gallery(Model model) {
        model.addAttribute("gallerylist", galleryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "frontview/gallery";
    }

    @RequestMapping("/galleryimage/{galleryid}")
    public String galleryimage(Model model, @PathVariable Long galleryid) {

        Gallery gallery = new Gallery();
        gallery.setId(galleryid);
        model.addAttribute("galleryimagelist", imageGalleryRepository.findByGalleryOrderByIdDesc(gallery));
        return "frontview/galleryimage";
    }

    @RequestMapping("/donations")
    public String donations(Model model) {
        model.addAttribute("attribute", "value");
        return "frontview/donations";
    }

    @RequestMapping("/services")
    public String services(Model model) {
        model.addAttribute("servicelist", ourservicesRepository.findByStatusOrderByIdDesc(com.ecommerce.app.model.enumvalue.Status.Active));
        return "frontview/services";
    }

    @RequestMapping("/services-details")
    public String services_details(Model model) {
        model.addAttribute("servicelist", ourservicesRepository.findByStatusOrderByIdDesc(com.ecommerce.app.model.enumvalue.Status.Active));
        return "frontview/services_details";
    }

    @RequestMapping("/clients")
    public String clients(Model model) {
        model.addAttribute("clientslist", ourclientsRepository.findByStatusOrderByIdDesc(com.ecommerce.app.model.enumvalue.Status.Active));
        return "frontview/clients";
    }

    @RequestMapping("/jobs")
    public String jobs(Model model) {
        model.addAttribute("attribute", "value");
        return "frontview/jobs";
    }

    @RequestMapping("/testimonial")
    public String testimonial(Model model) {
        model.addAttribute("attribute", "value");
        return "frontview/testimonial";
    }

    @RequestMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("faqlist", faqRepository.findByStatusOrderByIdDesc(com.ecommerce.app.model.enumvalue.Status.Active));
        return "frontview/faq";
    }

    @RequestMapping("/contactUs")
    public String contactUs(Model model) {
        model.addAttribute("attribute", "value");
        return "frontview/contactUs";
    }

    @RequestMapping("/home-contact-save")
    public String homecontactsave(Model model, @Valid Contact contact, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        model.addAttribute("attribute", "value");

        if (bindingResult.hasErrors()) {
            return "welcome/welcome";
        }
        contactRepository.save(contact);

        redirectAttributes.addFlashAttribute("message", "Your Message Successfully ");

        return "redirect:/";
    }

    //  Model test Start Here 
    @RequestMapping("/exam-category")
    public String examcategory(Model model) {

        model.addAttribute("categorylist", productcategoryRepository.findAll());

        return "frontview/exam-category";
    }

    @RequestMapping("/blog")
    public String blog(Model model, Users users) {
        model.addAttribute("bloglist", blogRepository.findByStatusOrderByIdDesc(com.ecommerce.app.model.enumvalue.Status.Active));
        model.addAttribute("blogcategorylist", blogCategoryRepository.findAll());

        return "frontview/blog";
    }

    @RequestMapping("/blogdetails/{blogid}")
    public String blogdetails(Model model, @PathVariable Long blogid, Users users) {

        model.addAttribute("bloglist", blogRepository.findByIdAndStatus(blogid, com.ecommerce.app.model.enumvalue.Status.Active));

        model.addAttribute("blogcategorylist", blogCategoryRepository.findAll());

        return "frontview/blogdetails";
    }

    @RequestMapping("/blog-by-cat/{catid}")
    public String blogByCat(Model model, @PathVariable Long catid, Users users) {

        BlogCategory blogCategory = new BlogCategory();

        blogCategory.setId(catid);

        model.addAttribute("bloglist", blogRepository.findByBlogcategoryAndStatusOrderByIdDesc(blogCategory, com.ecommerce.app.model.enumvalue.Status.Active));

        model.addAttribute("blogcategorylist", blogCategoryRepository.findAll());

        return "frontview/blog-by-category";
    }

    @RequestMapping("/privacy_policy")
    public String privacyPolicy() {
        return "frontview/privacy_policy";

    }

}
