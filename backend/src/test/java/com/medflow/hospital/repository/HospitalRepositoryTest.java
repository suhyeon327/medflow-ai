package com.medflow.hospital.repository;

import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.entity.HospitalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        seoulHospital = save("강남 메드 병원", "서울시 강남구 테헤란로", "서울", HospitalStatus.ACTIVE);
        busanHospital = save("부산 건강 병원", "부산시 해운대구", "부산", HospitalStatus.ACTIVE);
        closedHospital = save("서울 폐업 병원", "서울시 종로구", "서울", HospitalStatus.CLOSED);
    }

    @Test
    void findAllByStatus_returnsOnlyActiveHospitals() {
        var hospitals = hospitalRepository.findAllByStatus(HospitalStatus.ACTIVE);

        assertThat(hospitals).containsExactlyInAnyOrder(seoulHospital, busanHospital);
        assertThat(hospitals).doesNotContain(closedHospital);
    }

    @Test
    void searchByStatusAndKeyword_searchesHospitalName() {
        var hospitals = hospitalRepository.searchByStatusAndKeyword(HospitalStatus.ACTIVE, "강남 메드");

        assertThat(hospitals).containsExactly(seoulHospital);
    }

    @Test
    void searchByStatusAndKeyword_searchesRegionCaseInsensitively() {
        var hospitals = hospitalRepository.searchByStatusAndKeyword(HospitalStatus.ACTIVE, "부산");

        assertThat(hospitals).containsExactly(busanHospital);
    }

    @Test
    void searchByStatusAndKeyword_searchesAddressAndExcludesClosedHospital() {
        var hospitals = hospitalRepository.searchByStatusAndKeyword(HospitalStatus.ACTIVE, "서울시");

        assertThat(hospitals).containsExactly(seoulHospital);
        assertThat(hospitals).doesNotContain(closedHospital);
    }

    @Test
    void findByIdAndStatus_doesNotReturnClosedHospital() {
        assertThat(hospitalRepository.findByIdAndStatus(
                closedHospital.getId(), HospitalStatus.ACTIVE)).isEmpty();
    }

    private Hospital save(String name, String address, String region, HospitalStatus status) {
        return hospitalRepository.saveAndFlush(
                Hospital.create(name, address, region, "02-1234-5678", status)
        );
    }
}
