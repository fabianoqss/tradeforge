package com.fabiano.tradeforge.entities;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true)
	private String cpf;
	
	private String password;
	
	@Column(unique = true)
	private String name;
	
	@Column(unique = true)
	private String email;
	
	@Column(unique = true)
	private String nickname;

	@OneToOne
	@JoinColumn(name = "portfolio_id")
	private Portfolio portfolio;
	
	private List<Asset> assets;

	
	@ManyToMany
	@JoinTable(name = "tb_user_role",
				joinColumns = @JoinColumn(name = "user_id"),
				inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles =  new HashSet<>();
	
	public void addRole(Role role) {
		roles.add(role);
	}
	
	public boolean hasRole(String roleName) {
		for(Role role : roles) {
			if(role.getAuthority().equals(roleName)) {
				return true;
			}
		}
			return false;
	}
	
}
