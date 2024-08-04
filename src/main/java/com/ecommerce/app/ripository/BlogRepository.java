/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.ripository;

import com.ecommerce.app.model.Blog;
import com.ecommerce.app.model.BlogCategory;
import com.ecommerce.app.model.enumvalue.Status;
import com.ecommerce.app.module.user.model.Users;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author User
 */
public interface BlogRepository extends JpaRepository<Blog, Long> {

    List<Blog> findByUserIdOrderByIdDesc(Users userid);

    List<Blog> findByStatusOrderByIdDesc(Status status);

    Blog findByIdAndStatus(Long id, Status status);

    List<Blog> findByBlogcategoryAndStatusOrderByIdDesc(BlogCategory blogCategory, Status status);

}
