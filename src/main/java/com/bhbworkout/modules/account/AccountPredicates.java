package com.bhbworkout.modules.account;

import com.bhbworkout.modules.tag.Tag;
import com.bhbworkout.modules.zone.Zone;
import com.querydsl.core.types.Predicate;

import java.util.Set;


public class AccountPredicates {
    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones){
        QAccount qAccount = QAccount.account;

        // account가 들고 있는 zone 중에 아무거나 zone에 해당되고
        // tag 중에 매칭이 되는
       return qAccount.zones.any().in(zones).and(qAccount.tags.any().in(tags));

    }
}
