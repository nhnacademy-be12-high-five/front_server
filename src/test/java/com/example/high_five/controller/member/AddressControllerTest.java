package com.example.high_five.controller.member;

import com.example.high_five.dto.member.request.AddressRequest;
import com.example.high_five.dto.member.response.AddressListResponse;
import com.example.high_five.dto.member.response.AddressResponse;
import com.example.high_five.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    @InjectMocks
    private AddressController addressController;

    @Mock
    private AddressService addressService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(addressController).build();
    }

    @Test
    @DisplayName("배송지 목록 조회 - 정상 (데이터 있음)")
    void addressList_Success() throws Exception {
        // [수정] AddressResponse 생성자 (8개 인자)
        // addressId, alias, roadAddress, detailAddress, recipient, phone, zipCode, isDefault
        AddressResponse addr = new AddressResponse(
                1L,
                "Home",
                "Seoul Road",
                "101-202",
                "Hong Gil Dong",
                "010-1111-2222",
                "12345",
                false
        );
        AddressListResponse response = new AddressListResponse(List.of(addr));

        given(addressService.getAddressList()).willReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/mypage/address"))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/address"))
                .andExpect(model().attributeExists("addresses"));
    }

    @Test
    @DisplayName("배송지 목록 조회 - 예외 발생 시 빈 리스트")
    void addressList_Exception() throws Exception {
        given(addressService.getAddressList()).willThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/mypage/address"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("addresses", Collections.emptyList()));
    }

    @Test
    @DisplayName("배송지 등록 폼")
    void registerForm() throws Exception {
        mockMvc.perform(get("/mypage/address/register"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isUpdate", false));
    }

    @Test
    @DisplayName("배송지 등록 처리 - 성공")
    void registerAddress_Success() throws Exception {
        // [수정] 유효한 AddressRequest 생성
        AddressRequest validRequest = new AddressRequest(
                "Home", "Road 123", "Detail 101", "Recipient", "010-1234-5678", "12345", false
        );

        mockMvc.perform(post("/mypage/address/register")
                        .flashAttr("addressRequest", validRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage/address"))
                .andExpect(flash().attributeExists("message"));

        verify(addressService).createAddress(any(AddressRequest.class));
    }

    @Test
    @DisplayName("배송지 수정 폼 - 성공")
    void updateForm_Success() throws Exception {
        // [수정] AddressResponse 생성자
        AddressResponse addr = new AddressResponse(
                1L, "Home", "Road", "Detail", "Recipient", "010-0000-0000", "12345", false
        );
        given(addressService.getAddress(1L)).willReturn(ResponseEntity.ok(addr));

        mockMvc.perform(get("/mypage/address/1/update"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isUpdate", true))
                .andExpect(model().attribute("address", addr));
    }

    @Test
    @DisplayName("배송지 수정 처리 - 성공")
    void updateAddress_Success() throws Exception {
        // [수정] 유효한 AddressRequest 생성
        AddressRequest validRequest = new AddressRequest(
                "Office", "Road 456", "Detail 202", "Recipient", "010-9876-5432", "54321", true
        );

        mockMvc.perform(post("/mypage/address/1/update")
                        .flashAttr("addressRequest", validRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage/address"))
                .andExpect(flash().attributeExists("message"));

        verify(addressService).updateAddress(eq(1L), any(AddressRequest.class));
    }

    @Test
    @DisplayName("배송지 삭제 처리")
    void deleteAddress() throws Exception {
        mockMvc.perform(post("/mypage/address/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"));

        verify(addressService).deleteAddress(1L);
    }

    @Test
    @DisplayName("기본 배송지 설정")
    void setDefaultAddress() throws Exception {
        mockMvc.perform(post("/mypage/address/1/default"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"));

        verify(addressService).setDefaultAddress(1L);
    }
}