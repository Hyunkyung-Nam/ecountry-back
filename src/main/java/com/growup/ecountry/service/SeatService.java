package com.growup.ecountry.service;

import com.growup.ecountry.dto.SeatDTO;
import com.growup.ecountry.entity.Countries;
import com.growup.ecountry.entity.Seats;
import com.growup.ecountry.repository.CountryRepository;
import com.growup.ecountry.repository.SeatRepository;
import com.growup.ecountry.repository.SeatStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final CountryRepository countryRepository;
    private final SeatRepository seatRepository;
    private final SeatStatusRepository seatStatusRepository;

    public List<Seats> createSeat(List<SeatDTO> seatDTOS) {
        Countries countries = countryRepository.findById(seatDTOS.get(0).getCountryId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 국가입니다."));
        if(countries.getAvailable()){
            List<Seats> seats = seatDTOS.stream().map(seat -> Seats.builder().rowNum(seat.getRowNum())
                    .colNum(seat.getColNum()).countryId(seat.getCountryId()).build()).toList();
            return seatRepository.saveAll(seats);
        }
        else {
            return null;
        }
    }

    public List<SeatDTO> getSeat(Long countryId) {
        Countries countries = countryRepository.findById(countryId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 국가입니다."));
        if(countries.getAvailable()){
            return seatRepository.findByCountryId(countryId).stream().map(seat -> SeatDTO.builder()
                    .id(seat.getId()).rowNum(seat.getRowNum()).colNum(seat.getColNum())
                    .countryId(seat.getCountryId()).build()).collect(Collectors.toList());
        }
        else {
            return null;
        }
    }

    public List<Seats> updateSeat(List<SeatDTO> seatDTOS) {
        Countries countries = countryRepository.findById(seatDTOS.get(0).getCountryId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 국가입니다."));
        if(countries.getAvailable()){
            List<Seats> prevData = seatRepository.findByCountryId(seatDTOS.get(0).getCountryId());
            for (Seats data: prevData) {
                seatRepository.deleteById(data.getId());
            }
            //자리 배치 변경 시 해당 countryId를 가진 자리 사용 현황 table의 데이터 삭제
            seatStatusRepository.deleteAllByCountryId(seatDTOS.get(0).getCountryId());

            List<Seats> seats = seatDTOS.stream().map(seat -> Seats.builder().rowNum(seat.getRowNum())
                    .colNum(seat.getColNum()).countryId(seat.getCountryId()).build()).toList();
            return seatRepository.saveAll(seats);
        }
        else {
            return null;
        }
    }
}
