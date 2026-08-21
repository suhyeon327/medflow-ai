package com.medflow.hospital.repository;

import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.entity.HospitalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HospitalRepositoryTest {

    @Autowired
    private HospitalRepository hospitalRepository;

    private Hospital seoulHospital;
    private Hospital busanHospital;
    private Hospital closedHospital;

    @BeforeEach
    void setUp() {
        seoulHospital = save("Alpha 메드 병원", "서울시 강남구 테헤란로", "서울", HospitalStatus.ACTIVE);
        busanHospital = save("Beta 건강 병원", "부산시 해운대구", "부산", HospitalStatus.ACTIVE);
        closedHospital = save("Closed 폐업 병원", "서울시 종로구", "서울", HospitalStatus.CLOSED);
    }

    @Test
    void findAllByStatus_returnsOnlyHospitalsWithRequestedStatus() {
        var hospitals = hospitalRepository.findAllByStatus(
                HospitalStatus.ACTIVE,
                PageRequest.of(0, 20)
        ).getContent();

        assertThat(hospitals).containsExactly(seoulHospital, busanHospital);
        assertThat(hospitals).doesNotContain(closedHospital);
    }

    @Test
    void searchByStatusAndKeyword_searchesNameCaseInsensitively() {
        var hospitals = hospitalRepository.searchByStatusAndKeyword(
                HospitalStatus.ACTIVE,
                "ALPHA",
                PageRequest.of(0, 20)
        ).getContent();

        assertThat(hospitals).containsExactly(seoulHospital);
    }

    @Test
    void searchByStatusAndKeyword_searchesAddressAndExcludesClosedHospital() {
        var hospitals = hospitalRepository.searchByStatusAndKeyword(
                HospitalStatus.ACTIVE,
                "서울시",
                PageRequest.of(0, 20)
        ).getContent();

        assertThat(hospitals).containsExactly(seoulHospital);
        assertThat(hospitals).doesNotContain(closedHospital);
    }

    @Test
    void findAllByStatus_appliesPaginationAndReportsTotalElements() {
        var page = hospitalRepository.findAllByStatus(
                HospitalStatus.ACTIVE,
                PageRequest.of(1, 1)
        );

        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findByIdAndStatus_returnsActiveHospitalAndExcludesClosedHospital() {
        assertThat(hospitalRepository.findByIdAndStatus(
                seoulHospital.getId(), HospitalStatus.ACTIVE)).contains(seoulHospital);
        assertThat(hospitalRepository.findByIdAndStatus(
                closedHospital.getId(), HospitalStatus.ACTIVE)).isEmpty();
    }

    @Test
    void existsByName_returnsTrueForExistingNameAndFalseForUnknownName() {
        assertThat(hospitalRepository.existsByName("Alpha 메드 병원")).isTrue();
        assertThat(hospitalRepository.existsByName("없는 병원")).isFalse();
    }

    private Hospital save(String name, String address, String region, HospitalStatus status) {
        return hospitalRepository.saveAndFlush(
                new Hospital(name, address, region, "02-1234-5678", status)
        );
    }
}
