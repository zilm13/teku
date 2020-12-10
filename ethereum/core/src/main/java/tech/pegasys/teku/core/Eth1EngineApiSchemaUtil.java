/*
 * Copyright 2020 ConsenSys AG.
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

package tech.pegasys.teku.core;

import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.teku.datastructures.blocks.exec.Eth1Transaction;
import tech.pegasys.teku.datastructures.blocks.exec.ExecutableData;
import tech.pegasys.teku.exec.eth1engine.schema.Eth1TransactionDTO;
import tech.pegasys.teku.exec.eth1engine.schema.ExecutableDTO;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.SSZTypes.Bytes20;

public final class Eth1EngineApiSchemaUtil {

  public static ExecutableData parseExecutableDTO(ExecutableDTO executableDTO) {
    return new ExecutableData(
        Bytes32.fromHexString(executableDTO.getBlock_hash()),
        Bytes20.fromHexString(executableDTO.getCoinbase()),
        Bytes32.fromHexString(executableDTO.getState_root()),
        UInt64.valueOf(executableDTO.getGas_limit()),
        UInt64.valueOf(executableDTO.getGas_used()),
        Bytes32.fromHexString(executableDTO.getReceipt_root()),
        Bytes.fromHexString(executableDTO.getLogs_bloom()),
        UInt64.valueOf(executableDTO.getNumber()),
        UInt64.valueOf(executableDTO.getDifficulty()),
        executableDTO.getTransactions().stream()
            .map(Eth1EngineApiSchemaUtil::parseEth1TransactionDTO)
            .collect(Collectors.toList()));
  }

  public static ExecutableDTO getExecutableDTO(ExecutableData executableData) {
    return new ExecutableDTO(
        executableData.getBlock_hash().toHexString(),
        executableData.getCoinbase().toHexString(),
        executableData.getState_root().toHexString(),
        executableData.getGas_limit().longValue(),
        executableData.getGas_used().longValue(),
        executableData.getReceipt_root().toHexString(),
        executableData.getLogs_bloom().toHexString(),
        executableData.getNumber().longValue(),
        executableData.getDifficulty().bigIntegerValue(),
        executableData.getTransactions().stream()
            .map(Eth1EngineApiSchemaUtil::getEth1TransactionDTO)
            .collect(Collectors.toList()));
  }

  public static Eth1Transaction parseEth1TransactionDTO(Eth1TransactionDTO eth1TransactionDTO) {
    return new Eth1Transaction(
        UInt64.valueOf(eth1TransactionDTO.getNonce()),
        UInt256.valueOf(eth1TransactionDTO.getGas_price()),
        UInt64.valueOf(eth1TransactionDTO.getGas_limit()),
        Bytes20.fromHexString(eth1TransactionDTO.getRecipient()),
        UInt256.valueOf(eth1TransactionDTO.getValue()),
        Bytes.fromHexString(eth1TransactionDTO.getInput()),
        UInt256.valueOf(eth1TransactionDTO.getV()),
        UInt256.valueOf(eth1TransactionDTO.getR()),
        UInt256.valueOf(eth1TransactionDTO.getS()));
  }

  public static Eth1TransactionDTO getEth1TransactionDTO(Eth1Transaction eth1Transaction) {
    return new Eth1TransactionDTO(
        eth1Transaction.getNonce().longValue(),
        eth1Transaction.getGas_price().toBigInteger(),
        eth1Transaction.getGas_limit().longValue(),
        eth1Transaction.getRecipient().toHexString(),
        eth1Transaction.getValue().toBigInteger(),
        eth1Transaction.getInput().toHexString(),
        eth1Transaction.getV().toBigInteger(),
        eth1Transaction.getR().toBigInteger(),
        eth1Transaction.getS().toBigInteger());
  }
}
