package com.mikhaylov.diplom.repository;

import com.mikhaylov.diplom.repository.entity.CleanTextEntity;
import com.mikhaylov.diplom.repository.entity.RawTextEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CleanTextRepository extends JpaRepository<CleanTextEntity, Long> {

    Optional<CleanTextEntity> findByRawText(RawTextEntity rawText);
}
