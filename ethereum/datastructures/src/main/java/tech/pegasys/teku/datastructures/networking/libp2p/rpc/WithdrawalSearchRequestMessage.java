/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.datastructures.networking.libp2p.rpc;

import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.ssz.backing.containers.Container1;
import tech.pegasys.teku.ssz.backing.containers.ContainerSchema1;
import tech.pegasys.teku.ssz.backing.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.view.SszPrimitives;

// FIXME: we should choose some searchable index for withdrawal lookup instead,
// or confirm pubkey hash and store indexed data
public class WithdrawalSearchRequestMessage
    extends Container1<WithdrawalSearchRequestMessage, SszPrimitives.SszBytes32>
    implements RpcRequest {

  public static final WithdrawalSearchRequestMessageSchema SSZ_SCHEMA =
      new WithdrawalSearchRequestMessageSchema();

  private WithdrawalSearchRequestMessage(
      WithdrawalSearchRequestMessageSchema type, TreeNode backingNode) {
    super(type, backingNode);
  }

  public WithdrawalSearchRequestMessage(final Bytes32 pubkeyHash) {
    super(SSZ_SCHEMA, new SszPrimitives.SszBytes32(pubkeyHash));
  }

  public Bytes32 getPubkeyHash() {
    return getField0().get();
  }

  @Override
  public int getMaximumRequestChunks() {
    return 1;
  }

  public static class WithdrawalSearchRequestMessageSchema
      extends ContainerSchema1<WithdrawalSearchRequestMessage, SszPrimitives.SszBytes32> {

    public WithdrawalSearchRequestMessageSchema() {
      super(
          "WithdrawalSearchRequestMessage",
          namedSchema("pubkeyHash", SszPrimitiveSchemas.BYTES32_SCHEMA));
    }

    @Override
    public WithdrawalSearchRequestMessage createFromBackingNode(TreeNode node) {
      return new WithdrawalSearchRequestMessage(this, node);
    }
  }
}
