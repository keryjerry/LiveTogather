package com.devil.livetogather.agora;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
