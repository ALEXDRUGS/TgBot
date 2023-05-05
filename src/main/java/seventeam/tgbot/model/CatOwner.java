package seventeam.tgbot.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "cat_owners")
public class CatOwner extends User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_id")
    private Long id;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
    @Column(name = "pets", nullable = false)
    @OneToMany(mappedBy = "catOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cat> pets;

    public CatOwner() {
    }

    public CatOwner(Long id, String firstName, String lastName, String phoneNumber, List<Cat> pets) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.pets = pets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CatOwner catOwner = (CatOwner) o;
        return Objects.equals(id, catOwner.id) && firstName.equals(catOwner.firstName) && lastName.equals(catOwner.lastName) && phoneNumber.equals(catOwner.phoneNumber) && pets.equals(catOwner.pets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, firstName, lastName, phoneNumber, pets);
    }
}