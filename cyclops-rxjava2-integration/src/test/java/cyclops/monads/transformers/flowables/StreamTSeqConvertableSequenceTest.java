package cyclops.monads.transformers.flowables;


import com.oath.cyclops.types.foldable.AbstractConvertableSequenceTest;
import com.oath.cyclops.types.foldable.ConvertableSequence;
import cyclops.companion.rx2.Flowables;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness.list;


public class StreamTSeqConvertableSequenceTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {

        return AnyMs.liftM(Flowables.of(elements), list.INSTANCE).to();
    }

    @Override
    public <T> ConvertableSequence<T> empty() {

        return AnyMs.liftM(Flowables.<T>empty(),list.INSTANCE).to();
    }

}
