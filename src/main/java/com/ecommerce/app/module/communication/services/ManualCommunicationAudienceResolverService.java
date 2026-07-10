package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.ManualCommunicationActor;
import com.ecommerce.app.module.communication.dto.ManualMessageRequest;
import com.ecommerce.app.module.communication.dto.ManualRecipient;
import com.ecommerce.app.module.communication.model.ManualAudience;
import com.ecommerce.app.module.communication.model.ManualRecipientType;
import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManualCommunicationAudienceResolverService {

    private final UsersRepository usersRepository;
    private final VendorprofileRepository vendorprofileRepository;
    private final SalesOrderRepository salesOrderRepository;

    public ManualCommunicationAudienceResolverService(
            UsersRepository usersRepository,
            VendorprofileRepository vendorprofileRepository,
            SalesOrderRepository salesOrderRepository) {
        this.usersRepository = usersRepository;
        this.vendorprofileRepository = vendorprofileRepository;
        this.salesOrderRepository = salesOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<ManualRecipient> resolve(ManualCommunicationActor actor, ManualMessageRequest request) {
        ManualAudience audience = request.getAudience();
        if (audience == ManualAudience.ADMIN) {
            return adminRecipients();
        }
        if (audience == ManualAudience.ALL_CUSTOMERS) {
            return customerRecipients(usersRepository.findByUserTypeAndStatusOrderByIdDesc(UserType.customer, Status.Active));
        }
        if (audience == ManualAudience.SELECTED_CUSTOMERS) {
            return customerRecipients(usersRepository.findByIdsAndUserTypeAndStatus(normalizedIds(request), UserType.customer, Status.Active));
        }
        if (audience == ManualAudience.SINGLE_CUSTOMER) {
            return customerRecipients(usersRepository.findByIdsAndUserTypeAndStatus(singleId(request), UserType.customer, Status.Active));
        }
        if (audience == ManualAudience.ALL_VENDORS) {
            return vendorRecipients(vendorprofileRepository.findAll());
        }
        if (audience == ManualAudience.SELECTED_VENDORS) {
            return vendorRecipients(vendorprofileRepository.findAllById(normalizedIds(request)));
        }
        if (audience == ManualAudience.SINGLE_VENDOR) {
            return vendorRecipients(vendorprofileRepository.findAllById(singleId(request)));
        }
        if (audience == ManualAudience.OWN_CUSTOMERS) {
            return vendorCustomerRecipients(actor.getVendorId(), null);
        }
        if (audience == ManualAudience.SELECTED_OWN_CUSTOMERS) {
            return vendorCustomerRecipients(actor.getVendorId(), normalizedIds(request));
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public List<Users> selectableCustomersForAdmin() {
        return usersRepository.findByUserTypeAndStatusOrderByIdDesc(UserType.customer, Status.Active);
    }

    @Transactional(readOnly = true)
    public List<Vendorprofile> selectableVendorsForAdmin() {
        return vendorprofileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Users> selectableCustomersForVendor(Long vendorId) {
        if (vendorId == null) {
            return List.of();
        }
        return salesOrderRepository.findDistinctCustomersByVendorId(vendorId);
    }

    private List<ManualRecipient> adminRecipients() {
        return userRecipients(
                usersRepository.findByUserTypeInAndStatusOrderByIdDesc(List.of(UserType.administrator, UserType.systemadmin), Status.Active),
                ManualRecipientType.ADMIN
        );
    }

    private List<ManualRecipient> customerRecipients(Collection<Users> users) {
        return userRecipients(users, ManualRecipientType.CUSTOMER);
    }

    private List<ManualRecipient> vendorCustomerRecipients(Long vendorId, Collection<Long> selectedCustomerIds) {
        if (vendorId == null) {
            return List.of();
        }
        List<Users> users = selectedCustomerIds == null || selectedCustomerIds.isEmpty()
                ? salesOrderRepository.findDistinctCustomersByVendorId(vendorId)
                : salesOrderRepository.findDistinctCustomersByVendorIdAndCustomerIds(vendorId, selectedCustomerIds);
        return userRecipients(users, ManualRecipientType.CUSTOMER);
    }

    private List<ManualRecipient> userRecipients(Collection<Users> users, ManualRecipientType type) {
        Map<Long, ManualRecipient> recipients = new LinkedHashMap<>();
        if (users == null) {
            return List.of();
        }
        for (Users user : users) {
            if (user == null || user.getId() == null) {
                continue;
            }
            recipients.put(user.getId(), new ManualRecipient(
                    type,
                    user.getId(),
                    user,
                    userDisplayName(user),
                    user.getEmail(),
                    user.getMobile()
            ));
        }
        return new ArrayList<>(recipients.values());
    }

    private List<ManualRecipient> vendorRecipients(Iterable<Vendorprofile> vendors) {
        Map<Long, ManualRecipient> recipients = new LinkedHashMap<>();
        if (vendors == null) {
            return List.of();
        }
        for (Vendorprofile vendor : vendors) {
            if (vendor == null || vendor.getId() == null) {
                continue;
            }
            Users user = vendor.getUserId();
            String email = clean(vendor.getEmail()) != null ? vendor.getEmail() : (user == null ? null : user.getEmail());
            String phone = clean(vendor.getPhone()) != null ? vendor.getPhone() : (user == null ? null : user.getMobile());
            recipients.put(vendor.getId(), new ManualRecipient(
                    ManualRecipientType.VENDOR,
                    vendor.getId(),
                    user,
                    vendorDisplayName(vendor),
                    email,
                    phone
            ));
        }
        return new ArrayList<>(recipients.values());
    }

    private List<Long> normalizedIds(ManualMessageRequest request) {
        List<Long> ids = new ArrayList<>();
        if (request.getRecipientIds() != null) {
            request.getRecipientIds().stream().filter(Objects::nonNull).forEach(ids::add);
        }
        if (request.getRecipientId() != null) {
            ids.add(request.getRecipientId());
        }
        return ids.stream().distinct().toList();
    }

    private List<Long> singleId(ManualMessageRequest request) {
        if (request.getRecipientId() == null) {
            return List.of();
        }
        return List.of(request.getRecipientId());
    }

    private String userDisplayName(Users user) {
        String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return name.isBlank() ? user.getEmail() : name;
    }

    private String vendorDisplayName(Vendorprofile vendor) {
        String companyName = clean(vendor.getCompanyName());
        if (companyName != null) {
            return companyName;
        }
        String name = ((vendor.getFirstName() == null ? "" : vendor.getFirstName()) + " "
                + (vendor.getLastName() == null ? "" : vendor.getLastName())).trim();
        return name.isBlank() ? vendor.getEmail() : name;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
