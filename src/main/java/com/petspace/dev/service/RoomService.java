package com.petspace.dev.service;

import com.petspace.dev.domain.Facility;
import com.petspace.dev.domain.Reservation;
import com.petspace.dev.domain.Review;
import com.petspace.dev.domain.Room;
import com.petspace.dev.domain.image.RoomImage;
import com.petspace.dev.dto.oauth.OauthResponseDto;
import com.petspace.dev.dto.room.RoomCreateRequestDto;
import com.petspace.dev.dto.room.RoomDetailResponseDto;
import com.petspace.dev.repository.RoomImageRepository;
import com.petspace.dev.repository.RoomRepository;
import com.petspace.dev.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    private final RoomImageRepository roomImageRepository;

    // TODO
//    public int createRoom(RoomCreateRequestDto roomCreateRequestDto){
//        int result = 0;
//        Room room = roomCreateRequestDto.toEntity();
//        roomRepository.save(room);
//        return result;
//    }

    public RoomDetailResponseDto getRoomDetail(Long roomId) {

        // Room 가져오기. 현재는 Null 값일 수 없음을 가정한다.
        Optional<Room> roomList = roomRepository.findById(roomId);
        Room room = roomList.get();


        // Room 의 Image 리스트에서 URL 만을 List<String> 으로 받아오기
        List<String> imageUrlList = room.getRoomImages()
                .stream().map(RoomImage::getRoomImageUrl)
                .collect(Collectors.toList());

        // Room 의 주소 받아오기. TODO 주소 어느 형식으로 보내줄지 논의 필요
        String address = room.getAddress().getRegion() + " " + room.getAddress().getCity() + " " + room.getAddress().getAddressDetail();

        // 리뷰 평점 가져오기
        double review_rate = room.getReservation()
                .stream().map(Reservation::getReview)
                .map(Review::getScore)
                .mapToDouble(n -> n)
                .average()
                .orElse(0);

        // 리뷰 개수 가져오기
        long review_count = room.getReservation()
                .stream().map(Reservation::getReview)
                .count();

        // 리뷰 미리보기 5개 가져오기
        List<Review> reviewPreview = room.getReservation()
                .stream().map(Reservation::getReview)
                .collect(Collectors.toList());
        Comparator<Review> comparator = (r1, r2) -> Long.valueOf(
                        r1.getId())
                .compareTo(r2.getId());

        Collections.sort(reviewPreview, comparator.reversed());
        reviewPreview.stream().limit(5).collect(Collectors.toList()); // stream limit 은 없을 때 Exception 발생하지 않음.

        // Room 의 편의시설 받아오기

        return RoomDetailResponseDto // (room);
                .builder()
                .id(room.getId())
                .host(room.getUser().getUsername())
                .roomImageUrl(imageUrlList)
                .address(address)
                .name(room.getRoomName())
                .price(room.getPrice())
                .maxGuest(room.getMaxGuest())
                .maxPet(room.getMaxPet())
                .decription(room.getDescription())
                .checkinTime(room.getCheckinTime())
                .checkoutTime(room.getCheckoutTime())
                .rating(review_rate)
                .reviewCount(review_count)
                .reviewPreview(reviewPreview)
                .facilities(new ArrayList<>()) // TODO 연관관계 매핑 이후 진행
                .build();
    }

}
