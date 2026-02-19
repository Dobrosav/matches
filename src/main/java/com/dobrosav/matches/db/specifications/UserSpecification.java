package com.dobrosav.matches.db.specifications;

import com.dobrosav.matches.db.entities.User;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class UserSpecification {

    public static Specification<User> filter(Collection<Integer> excludedIds, String gender, Integer minAge, Integer maxAge, String location) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (excludedIds != null && !excludedIds.isEmpty()) {
                predicates.add(criteriaBuilder.not(root.get("id").in(excludedIds)));
            }

            if (gender != null && !gender.isEmpty() && !gender.equalsIgnoreCase("Any")) {
                predicates.add(criteriaBuilder.equal(root.get("sex"), gender));
            }

            Date now = new Date();
            // We use Joda Time for easy calculation, then convert to java.util.Date
            DateTime dateTimeNow = new DateTime(now);

            if (minAge != null && maxAge != null) {
                Date latestBirthDate = dateTimeNow.minusYears(minAge).toDate();
                Date earliestBirthDate = dateTimeNow.minusYears(maxAge + 1).plusDays(1).toDate();
                
                predicates.add(criteriaBuilder.between(root.get("dateOfBirth"), earliestBirthDate, latestBirthDate));
            } else if (minAge != null) {
                Date latestBirthDate = dateTimeNow.minusYears(minAge).toDate();
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateOfBirth"), latestBirthDate));
            } else if (maxAge != null) {
                Date earliestBirthDate = dateTimeNow.minusYears(maxAge + 1).plusDays(1).toDate();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateOfBirth"), earliestBirthDate));
            }

            if (location != null && !location.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("location"), location));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
