package com.fabiano.tradeforge.projections;

public interface UserDetailsProjection {

    String getUsername();
    String getPassword();
    Long getRoleId();
    String getAuthority();
    Boolean getEnabled();

}
