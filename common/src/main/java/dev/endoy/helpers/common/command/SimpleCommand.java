package dev.endoy.helpers.common.command;

import java.util.List;

public interface SimpleCommand<CS>
{

    void onCommand( CS commandSender, List<String> args );

}
