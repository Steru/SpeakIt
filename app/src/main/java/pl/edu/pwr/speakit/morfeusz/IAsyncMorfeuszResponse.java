package pl.edu.pwr.speakit.morfeusz;

import java.util.List;

import pl.edu.pwr.speakit.common.CommandDO;

/**
 * Created by Steru on 2017-01-24.
 */
public interface IAsyncMorfeuszResponse {
    void responseFinished(List<CommandDO> commandList);
}
