package ru.runa.wfe.chat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.runa.wfe.chat.dao.ChatFileIo;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alekseev Mikhail
 * @since #2199
 */
@RunWith(MockitoJUnitRunner.class)
public class ChatUnusedFilesCleanerJobTest {
    @Mock
    private ChatFileIo fileIo;
    @Mock
    private ChatUnusedFilesCleanerJob self;
    @InjectMocks
    private ChatUnusedFilesCleanerJob job;

    @Test
    public void givenNoFilesUsed_thenAllDeleted() throws IOException {
        when(self.getUsedFileUuids()).thenReturn(emptySet());
        when(fileIo.getFilesOlderThan(any())).thenReturn(mockStream("uuid1", "uuid2", "uuid3"));

        job.execute();

        verify(fileIo).deleteByUuid(eq("uuid1"));
        verify(fileIo).deleteByUuid(eq("uuid2"));
        verify(fileIo).deleteByUuid(eq("uuid3"));
    }

    @Test
    public void givenSomeFilesUsed_thenDeletedUnusedOnly() throws IOException {
        when(self.getUsedFileUuids()).thenReturn(newHashSet("uuid2"));
        when(fileIo.getFilesOlderThan(any())).thenReturn(mockStream("uuid1", "uuid2", "uuid3"));

        job.execute();

        verify(fileIo).deleteByUuid(eq("uuid1"));
        verify(fileIo, never()).deleteByUuid(eq("uuid2"));
        verify(fileIo).deleteByUuid(eq("uuid3"));
    }

    @Test
    public void givenAllFilesUsed_thenNoFilesDeleted() throws IOException {
        when(self.getUsedFileUuids()).thenReturn(newHashSet("uuid1", "uuid2", "uuid3"));
        when(fileIo.getFilesOlderThan(any())).thenReturn(mockStream("uuid1", "uuid2", "uuid3"));

        job.execute();

        verify(fileIo, never()).deleteByUuid(eq("uuid1"));
        verify(fileIo, never()).deleteByUuid(eq("uuid2"));
        verify(fileIo, never()).deleteByUuid(eq("uuid3"));
    }

    private static Stream<Path> mockStream(String... uuids) {
        return stream(uuids).map(uuid -> {
            final Path path = mock(Path.class);
            final Path fileNameMock = mock(Path.class);
            when(fileNameMock.toString()).thenReturn(uuid);
            when(path.getFileName()).thenReturn(fileNameMock);
            return path;
        });
    }
}