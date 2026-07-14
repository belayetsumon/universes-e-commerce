package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogApproval;
import com.ecommerce.app.module.blog.model.BlogApprovalDecision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogApprovalRepository extends JpaRepository<BlogApproval, Long> {

    Page<BlogApproval> findByDecisionOrderByCreatedAtDesc(BlogApprovalDecision decision, Pageable pageable);
}
