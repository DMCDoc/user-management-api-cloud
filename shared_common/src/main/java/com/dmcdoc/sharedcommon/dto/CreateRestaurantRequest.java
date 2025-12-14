/*
✔️ Impossible pour un client de tricher
✔️ API contract clair
✔️ Future-proof
 */

package com.dmcdoc.sharedcommon.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateRestaurantRequest {

    @NotBlank
    private String name;

    private String address;

    private String metadata;
    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public String getMetadata() {
        return metadata;
    }
}
