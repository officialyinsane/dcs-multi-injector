package uk.co.obora.dcs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.GenerationType.IDENTITY;

@AllArgsConstructor
@Builder
@Entity
@Getter
@NoArgsConstructor
@Setter
@Table(name = "dcs_server", schema = "injector", catalog = "injector")
public class DcsServer { // TODO: Entity - should really have more constraints

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name;

    @Column(name = "hostname", nullable = false, length = 50)
    private String hostname;

    @Column(name = "port", nullable = false)
    private int port;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    public static DcsServer getDefaultInstallationDetails() {
        return DcsServer.builder()
            .name("Localhost")
            .hostname("localhost")
            .port(18080)
            .password("default")
            .build();
    }

    public static DcsServer getBlankInstance() {
        return DcsServer.builder()
            .name("")
            .hostname("")
            .port(0)
            .password("")
            .build();
    }
}
