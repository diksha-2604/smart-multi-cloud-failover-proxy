
package com.smcfp.gateway.loadbalancer;

import com.smcfp.gateway.model.Backend;
import java.util.List;

public interface LoadBalancingStrategy {

    Backend select(List<Backend> backends);

}
