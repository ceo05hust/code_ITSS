package com.aims.aimsbackend.repository.product;

import com.aims.aimsbackend.entity.product.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
}
