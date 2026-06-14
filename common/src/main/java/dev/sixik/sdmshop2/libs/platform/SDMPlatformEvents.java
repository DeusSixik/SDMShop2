package dev.sixik.sdmshop2.libs.platform;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.sixik.sdmshop2.libs.platform.utils.repository.RepositoryType;
import dev.sixik.sdmshop2.libs.platform.utils.repositoryManager.RepositoryManager;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class SDMPlatformEvents {

    public static final Event<Invoker<RepositoryEvent>> REPOSITORY_CREATION =
            EventFactory.createLoop();

    @FunctionalInterface
    public interface Invoker<T> {

        void invoke(T value);
    }

    public static class RepositoryEvent {

        @Getter
        @Setter
        @Nullable
        private RepositoryManager manager;

        @Getter
        private final RepositoryType type;

        @Getter
        private final String creatorName;

        public RepositoryEvent(String creatorName, final RepositoryType type, @Nullable RepositoryManager manager) {
            this.creatorName = creatorName;
            this.manager = manager;
            this.type = type;
        }
    }
}
