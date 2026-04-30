package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "locations")
public class Location {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(name = "street_address")
    private String streetAddress;    

    private String ward;            

    private String district;        

    @Column(name = "province_city")
    private String provinceCity;     

    private String country;         

	@Column(name = "lat")
	private BigDecimal lat;

	@Column(name = "lng")
	private BigDecimal lng;

}


