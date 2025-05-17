package com.mikhaylov.diplom.repository;

import com.mikhaylov.diplom.repository.entity.RawTextEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RawTextRepository extends JpaRepository<RawTextEntity, Long> {

    Optional<RawTextEntity> findByRawText(String rawTextString);

    Optional<RawTextEntity> findFirstBySourceUrl(String sourceUrl);
}