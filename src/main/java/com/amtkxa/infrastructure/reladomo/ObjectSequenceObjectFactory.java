package com.amtkxa.infrastructure.reladomo;

import com.amtkxa.domain.entity.ObjectSequence;
import com.amtkxa.domain.entity.ObjectSequenceFinder;
import com.gs.fw.common.mithra.MithraSequence;
import com.gs.fw.common.mithra.MithraSequenceObjectFactory;

public class ObjectSequenceObjectFactory implements MithraSequenceObjectFactory {
    public MithraSequence getMithraSequenceObject(String sequenceName, Object sourceAttribute,
                                                  int initialValue) {
        ObjectSequence objectSequence = ObjectSequenceFinder.findByPrimaryKey(sequenceName);
        if (objectSequence == null) {
            objectSequence = new ObjectSequence();
            objectSequence.setSequenceName(sequenceName);
            objectSequence.setNextValue(initialValue);
            objectSequence.insert();
        }
        return objectSequence;
    }
}