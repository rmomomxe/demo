package com.example.demo.controller;

import com.example.demo.dto.CustomerDTO;
import jooqdata.tables.Customer;
import jooqdata.tables.records.CustomerRecord;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private DSLContext dsl;

    @GetMapping
    public List<CustomerDTO> getAllCustomers() {
        return dsl.selectFrom(Customer.CUSTOMER)
                .fetch()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Integer customerId) {
        try {
            CustomerRecord customer = dsl.selectFrom(Customer.CUSTOMER)
                    .where(Customer.CUSTOMER.CUSTOMER_ID.eq(customerId))
                    .fetchOne();
            if (customer == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(convertToDTO(customer));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customerDTO) {
        try {
            CustomerRecord newCustomer = dsl.insertInto(Customer.CUSTOMER)
                    .set(Customer.CUSTOMER.CUSTOMER_CODE, customerDTO.getCustomerCode())
                    .set(Customer.CUSTOMER.CUSTOMER_NAME, customerDTO.getCustomerName())
                    .set(Customer.CUSTOMER.CUSTOMER_INN, customerDTO.getCustomerInn())
                    .set(Customer.CUSTOMER.CUSTOMER_KPP, customerDTO.getCustomerKpp())
                    .set(Customer.CUSTOMER.CUSTOMER_LEGAL_ADDRESS, customerDTO.getCustomerLegalAddress())
                    .set(Customer.CUSTOMER.CUSTOMER_POSTAL_ADDRESS, customerDTO.getCustomerPostalAddress())
                    .set(Customer.CUSTOMER.CUSTOMER_EMAIL, customerDTO.getCustomerEmail())
                    .set(Customer.CUSTOMER.CUSTOMER_CODE_MAIN, customerDTO.getCustomerCodeMain())
                    .set(Customer.CUSTOMER.IS_ORGANIZATION, customerDTO.getIsOrganization())
                    .set(Customer.CUSTOMER.IS_PERSON, customerDTO.getIsPerson())
                    .returning()
                    .fetchOne();
            return ResponseEntity.ok(convertToDTO(newCustomer));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Integer customerId, @RequestBody CustomerDTO customerDTO) {
        try {
            if (customerDTO.getCustomerCodeMain() != null) {
                boolean exists = dsl.fetchExists(
                        dsl.selectFrom(Customer.CUSTOMER)
                                .where(Customer.CUSTOMER.CUSTOMER_CODE.eq(customerDTO.getCustomerCodeMain()))
                );
                if (!exists) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
            }
            int updated = dsl.update(Customer.CUSTOMER)
                    .set(Customer.CUSTOMER.CUSTOMER_CODE, customerDTO.getCustomerCode())
                    .set(Customer.CUSTOMER.CUSTOMER_NAME, customerDTO.getCustomerName())
                    .set(Customer.CUSTOMER.CUSTOMER_INN, customerDTO.getCustomerInn())
                    .set(Customer.CUSTOMER.CUSTOMER_KPP, customerDTO.getCustomerKpp())
                    .set(Customer.CUSTOMER.CUSTOMER_LEGAL_ADDRESS, customerDTO.getCustomerLegalAddress())
                    .set(Customer.CUSTOMER.CUSTOMER_POSTAL_ADDRESS, customerDTO.getCustomerPostalAddress())
                    .set(Customer.CUSTOMER.CUSTOMER_EMAIL, customerDTO.getCustomerEmail())
                    .set(Customer.CUSTOMER.CUSTOMER_CODE_MAIN, customerDTO.getCustomerCodeMain())
                    .set(Customer.CUSTOMER.IS_ORGANIZATION, customerDTO.getIsOrganization())
                    .set(Customer.CUSTOMER.IS_PERSON, customerDTO.getIsPerson())
                    .where(Customer.CUSTOMER.CUSTOMER_ID.eq(customerId))
                    .execute();
            if (updated == 0) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(customerDTO);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Integer customerId) {
        try {
            int deleted = dsl.deleteFrom(Customer.CUSTOMER)
                    .where(Customer.CUSTOMER.CUSTOMER_ID.eq(customerId))
                    .execute();
            if (deleted == 0) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private CustomerDTO convertToDTO(CustomerRecord record) {
        return new CustomerDTO(
                record.getCustomerId(),
                record.getCustomerCode(),
                record.getCustomerName(),
                record.getCustomerInn(),
                record.getCustomerKpp(),
                record.getCustomerLegalAddress(),
                record.getCustomerPostalAddress(),
                record.getCustomerEmail(),
                record.getCustomerCodeMain(),
                record.getIsOrganization(),
                record.getIsPerson()
        );
    }
}