package com.example.demo.controller;

import com.example.demo.dto.LotDTO;
import jooqdata.tables.Lot;
import jooqdata.tables.Customer; // Добавлен импорт
import jooqdata.tables.records.LotRecord;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException; // Добавлен импорт
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lots")
public class LotController {

    @Autowired
    private DSLContext dsl;

    @GetMapping
    public List<LotDTO> getAllLots() {
        return dsl.selectFrom(Lot.LOT)
                .fetch()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{lotId}")
    public ResponseEntity<LotDTO> getLotById(@PathVariable Integer lotId) {
        try {
            LotRecord lot = dsl.selectFrom(Lot.LOT)
                    .where(Lot.LOT.LOT_ID.eq(lotId))
                    .fetchOne();
            if (lot == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(convertToDTO(lot));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<LotDTO> createLot(@RequestBody LotDTO lotDTO) {
        try {
            // Проверяем, существует ли customer_code
            boolean exists = dsl.fetchExists(
                    dsl.selectFrom(Customer.CUSTOMER)
                            .where(Customer.CUSTOMER.CUSTOMER_CODE.eq(lotDTO.getCustomerCode()))
            );
            if (!exists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            LotRecord newLot = dsl.insertInto(Lot.LOT)
                    .set(Lot.LOT.LOT_NAME, lotDTO.getLotName())
                    .set(Lot.LOT.CUSTOMER_CODE, lotDTO.getCustomerCode())
                    .set(Lot.LOT.PRICE, lotDTO.getPrice())
                    .set(Lot.LOT.CURRENCY_CODE, lotDTO.getCurrencyCode())
                    .set(Lot.LOT.NDS_RATE, lotDTO.getNdsRate())
                    .set(Lot.LOT.PLACE_DELIVERY, lotDTO.getPlaceDelivery())
                    .set(Lot.LOT.DATE_DELIVERY, lotDTO.getDateDelivery())
                    .returning()
                    .fetchOne();
            return ResponseEntity.ok(convertToDTO(newLot));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/{lotId}")
    public ResponseEntity<LotDTO> updateLot(@PathVariable Integer lotId, @RequestBody LotDTO lotDTO) {
        try {
            // Проверяем, существует ли customer_code
            boolean exists = dsl.fetchExists(
                    dsl.selectFrom(Customer.CUSTOMER)
                            .where(Customer.CUSTOMER.CUSTOMER_CODE.eq(lotDTO.getCustomerCode()))
            );
            if (!exists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            int updated = dsl.update(Lot.LOT)
                    .set(Lot.LOT.LOT_NAME, lotDTO.getLotName())
                    .set(Lot.LOT.CUSTOMER_CODE, lotDTO.getCustomerCode())
                    .set(Lot.LOT.PRICE, lotDTO.getPrice())
                    .set(Lot.LOT.CURRENCY_CODE, lotDTO.getCurrencyCode())
                    .set(Lot.LOT.NDS_RATE, lotDTO.getNdsRate())
                    .set(Lot.LOT.PLACE_DELIVERY, lotDTO.getPlaceDelivery())
                    .set(Lot.LOT.DATE_DELIVERY, lotDTO.getDateDelivery())
                    .where(Lot.LOT.LOT_ID.eq(lotId))
                    .execute();
            if (updated == 0) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(lotDTO);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/{lotId}")
    public ResponseEntity<Void> deleteLot(@PathVariable Integer lotId) {
        try {
            int deleted = dsl.deleteFrom(Lot.LOT)
                    .where(Lot.LOT.LOT_ID.eq(lotId))
                    .execute();
            if (deleted == 0) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private LotDTO convertToDTO(LotRecord record) {
        return new LotDTO(
                record.getLotId(),
                record.getLotName(),
                record.getCustomerCode(),
                record.getPrice(),
                record.getCurrencyCode(),
                record.getNdsRate(),
                record.getPlaceDelivery(),
                record.getDateDelivery()
        );
    }
}