package com.comunidade.app.application.ports.in;

import com.comunidade.app.application.core.domain.BulkVeiculoRequest;
import com.comunidade.app.application.core.domain.BulkVeiculoResponse;

public interface BulkVeiculoServicePort {
    BulkVeiculoResponse procesarBulk(BulkVeiculoRequest request);
}

