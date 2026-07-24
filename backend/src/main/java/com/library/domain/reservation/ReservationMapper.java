package com.library.domain.reservation;

import com.library.domain.reservation.dto.ReservationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberCode", source = "member.memberCode")
    @Mapping(target = "memberName", source = "member.fullName")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "reservationDate", source = "createdAt")
    @Mapping(target = "heldCopyId", source = "heldCopy.id")
    @Mapping(target = "heldCopyBarcode", source = "heldCopy.barcode")
    ReservationResponse toResponse(Reservation reservation);
}
