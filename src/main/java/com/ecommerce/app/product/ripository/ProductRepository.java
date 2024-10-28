/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.model.enumvalue.Status;
import com.ecommerce.app.module.user.model.Users;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author User
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

//    List<Product> findByProductsubcategoryOrderByIdDesc(Productsubcategory productsubcategory);

    List<Product> findByUserIdOrderByIdDesc(Users userid);

    List<Product> findByUserIdOrderByIdDesc(Users userid, Pageable pageable);

    List<Product> findByUserIdAndStatusOrderByIdDesc(Users userid, Status status);

    List<Product> findByStatusOrderByIdDesc(Status status);

//    List<Product> findByUserIdAndOrderItemSalesOrderStatus(Pageable pageable, Users userId, OrderStatus orderStatus);

//    //Bestseller
//    @Query(
//            value = "SELECT e.*,COUNT(o.exam_id) AS orderitems FROM exam e "
//            + "INNER JOIN order_item o "
//            + "ON  e.id=o.exam_id "
//            + "GROUP BY  e.id "
//            + " ORDER  BY  orderitems  DESC ",
//            nativeQuery = true
//    )
//    List<Product> findByBestSeller(Pageable pageable);
//    
//        //Top Rated
//    
//        @Query(
//            value = "SELECT e.*,SUM(r.ratenumber) AS toprate FROM exam e "
//            + "INNER JOIN rate r "
//            + "ON  e.id=r.exam_id "
//            + "GROUP BY  e.id "
//            + " ORDER  BY  toprate  DESC ",
//            nativeQuery = true
//    )
//    List<Product> findByTopRated(Pageable pageable);
//    
//    
//    
//      //Best Instructor
//    @Query(
//            value = "SELECT e.*,COUNT(o.exam_id) AS orderitems FROM exam e "
//            + "INNER JOIN order_item o "
//            + "ON  e.id=o.exam_id "
//            + "GROUP BY  e.user_id_id "
//            + " ORDER  BY  orderitems  DESC ",
//            nativeQuery = true
//    )
//    List<Product> findByBestInstructor(Pageable pageable);

}
