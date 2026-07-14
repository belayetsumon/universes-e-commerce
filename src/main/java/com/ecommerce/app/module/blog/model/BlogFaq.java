package com.ecommerce.app.module.blog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(
        name = "blog_faqs",
        indexes = {
            @Index(name = "idx_blog_faq_blog", columnList = "blog_id,sort_order")
        }
)
public class BlogFaq extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @NotBlank(message = "FAQ question is required.")
    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @NotBlank(message = "FAQ answer is required.")
    @Lob
    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
