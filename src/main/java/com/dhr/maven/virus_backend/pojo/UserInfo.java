package com.dhr.maven.virus_backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "users")
@Data // 自动生成 Getter、Setter、toString、equals、hashCode
@NoArgsConstructor // 无参构造器
@AllArgsConstructor // 全参构造器
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
}
