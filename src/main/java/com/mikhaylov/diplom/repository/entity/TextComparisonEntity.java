package com.mikhaylov.diplom.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "text_comparisons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextComparisonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clean_text1_id", nullable = false)
    private CleanTextEntity cleanText1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clean_text2_id", nullable = false)
    private CleanTextEntity cleanText2;

    @Column(name = "similarity_score", nullable = false)
    private Double similarityScore;

    @Column(name = "detailed_report", columnDefinition = "TEXT")
    private String detailedReport;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
